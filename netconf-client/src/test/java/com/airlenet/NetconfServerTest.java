package com.airlenet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.session.ClientSessionImpl;
import org.apache.sshd.client.session.SessionFactory;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoAcceptor;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.netty.NettyIoServiceFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PublicKey;

public class NetconfServerTest implements ServerKeyVerifier, SessionListener {
    EventLoopGroup eventLoopGroup;
    public void init() throws IOException {
          eventLoopGroup = new DefaultEventLoopGroup();
        SshClient client = SshClient.setUpDefaultClient();
         client.setServerKeyVerifier(this);
         client.addSessionListener(this);
        NettyIoServiceFactory nettyIoServiceFactory = new NettyIoServiceFactory();
        IoAcceptor acceptor = nettyIoServiceFactory.createAcceptor(new SessionFactory(client) {
            @Override
            protected ClientSessionImpl createSession(IoSession ioSession) throws Exception {
                return super.createSession(ioSession);
            }
        });
        client.start();
        acceptor.bind(new InetSocketAddress(4344));

    }

    public static void main(String[] args) throws IOException {
        new NetconfServerTest().init();
    }

    @Override
    public boolean verifyServerKey(ClientSession clientSession, SocketAddress socketAddress, PublicKey publicKey) {


        return true;
    }

    @Override
    public void sessionEstablished(Session session) {

    }

    @Override
    public void sessionEvent(Session session, Event event) {
        switch (event){
            case KeyEstablished:
            doAuth((ClientSession) session);
            break;
            case Authenticated:
                doOpenNetconf((ClientSession)session);
                break;
            case KexCompleted:
                break;
            default:
                break;
        }

    }

    private void doOpenNetconf(ClientSession session) {
        final ClientChannel netconfChannel;
        try {
            netconfChannel = session.createSubsystemChannel("netconf");
            netconfChannel.setStreaming(ClientChannel.Streaming.Async);
            netconfChannel.open().addListener(new SshFutureListener<OpenFuture>() {
                @Override
                public void operationComplete(OpenFuture openFuture) {
                    if(openFuture.isOpened()){

                        AbstractServerChannel serverChannel = new AbstractServerChannel() {

                            @Override
                            public ChannelConfig config() {
                                return new DefaultChannelConfig(this);
                            }

                            @Override
                            public boolean isOpen() {
                                return false;
                            }

                            @Override
                            public boolean isActive() {
                                return false;
                            }

                            @Override
                            protected boolean isCompatible(EventLoop eventLoop) {
                                return true;
                            }

                            @Override
                            protected SocketAddress remoteAddress0() {

                                    return session.getIoSession().getRemoteAddress();
                            }

                            @Override
                            protected SocketAddress localAddress0() {
                                return session.getIoSession().getLocalAddress();
                            }

                            @Override
                            protected void doBind(SocketAddress socketAddress) throws Exception {
                                throw new UnsupportedOperationException("Bind not supported.");
                            }

                            @Override
                            protected void doClose() throws Exception {

                            }

                            @Override
                            protected void doBeginRead() throws Exception {

                            }
                        };

                        serverChannel.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {

                            private   Buffer toBuffer(final ByteBuf msg) {
                                // TODO Buffer vs ByteBuf translate, Can we handle that better ?
                                msg.resetReaderIndex();
                                final byte[] temp = new byte[msg.readableBytes()];
                                msg.readBytes(temp, 0, msg.readableBytes());
                                return new ByteArrayBuffer(temp);
                            }
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                final ByteBuf byteBufMsg = (ByteBuf) msg;
                                netconfChannel.getAsyncIn().writeBuffer(toBuffer(byteBufMsg)).addListener(new SshFutureListener<IoWriteFuture>() {
                                    @Override
                                    public void operationComplete(IoWriteFuture ioWriteFuture) {
                                        if(ioWriteFuture.isWritten()){

                                        }
                                    }
                                });
                            }
                        });
                        serverChannel.pipeline().addLast(new NetconfDelimiter());
                        eventLoopGroup.register(serverChannel);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doAuth(ClientSession session){
        session.setUsername("admin");
        session.addPasswordIdentity("admin");
        try {
            AuthFuture auth = session.auth();
            auth.addListener(new SshFutureListener<AuthFuture>() {
                @Override
                public void operationComplete(AuthFuture authFuture) {
                    if(authFuture.isSuccess()){

                    }
                    authFuture.removeListener(this);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static class NetconfDelimiter extends DelimiterBasedFrameDecoder {

        public static final ByteBuf DELIMITER = Unpooled.wrappedBuffer("]]>]]>".getBytes());

        public NetconfDelimiter() {
            super(Integer.MAX_VALUE, DELIMITER);
        }
    }

}
