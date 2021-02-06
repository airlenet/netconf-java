package com.airlenet;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.EventExecutor;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NetconfNettyClient {
    public static void main(String[] args) {
        io.netty.channel.EventLoopGroup globalBossGroup = null;
        io.netty.channel.EventLoopGroup    globalWorkerGroup =new NioEventLoopGroup();
         EventExecutor executor = new DefaultEventLoop();
        final Bootstrap b = new Bootstrap();
        b.group(globalWorkerGroup);
        InetSocketAddress address = new InetSocketAddress(2022);
        SshClient sshClient = SshClient.setUpDefaultClient();

        final NetconfSessionPromise<NetconfSession> p = new NetconfSessionPromise(executor,address,sshClient,b);
        b.option(ChannelOption.SO_KEEPALIVE,true).handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {

                ch.pipeline().addFirst(new ChannelOutboundHandlerAdapter(){

                    private ClientSession session;
                    private ChannelPromise connectPromise;    private ChannelSubsystem channel;
                    @Override
                    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
                        super.connect(ctx, remoteAddress, localAddress, promise);
                        connectPromise =promise;
                        startSSh(ctx, remoteAddress);
                    }
                    void startSSh(final ChannelHandlerContext ctx, final SocketAddress address){
                        final ConnectFuture sshConnectionFuture;
                        try {
                            sshConnectionFuture = sshClient.connect("admin", address)
                                    .verify(ctx.channel().config().getConnectTimeoutMillis(), TimeUnit.MILLISECONDS);
                            sshConnectionFuture.addListener(future -> {
                                if (future.isConnected()) {
                                    handleSshSessionCreated(future, ctx);
                                } else {
                                    handleSshSetupFailure(ctx, future.getException());
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    private synchronized void handleSshSessionCreated(final ConnectFuture future, final ChannelHandlerContext ctx) {
                        session = future.getSession();
                        session.addPasswordIdentity("admin");
                        try {
                            final AuthFuture authenticateFuture =   session.auth();
                            authenticateFuture.addListener(future1 -> {
                                if (future1.isSuccess()) {
                                    handleSshAuthenticated(session, ctx);
                                } else {
//                                    handleSshSetupFailure(ctx, new AuthenticationFailedException("Authentication failed",
//                                            future1.getException()));
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    private synchronized void handleSshAuthenticated(final ClientSession newSession,
                                                                     final ChannelHandlerContext ctx) {
                        try {
                            channel = newSession.createSubsystemChannel("netconf");
                            channel.setStreaming(ClientChannel.Streaming.Async);
                            channel.open().addListener(future -> {
                                if (future.isOpened()) {
                                    handleSshChanelOpened(ctx);
                                } else {
                                    handleSshSetupFailure(ctx, future.getException());
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    private synchronized void handleSshChanelOpened(final ChannelHandlerContext ctx) {

//                        sshWriteAsyncHandler = new AsyncSshHandlerWriter(channel.getAsyncIn());
                        ctx.fireChannelActive();
                        channel.onClose(() -> disconnect(ctx, ctx.newPromise()));
                    }
                    private synchronized void handleSshSetupFailure(final ChannelHandlerContext ctx, final Throwable error) {


                        // If the promise is not yet done, we have failed with initial connect and set connectPromise to failure
                        if (!connectPromise.isDone()) {
                            connectPromise.setFailure(error);
                        }

                        disconnect(ctx, ctx.newPromise());
                    }
                    public synchronized void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
//                        sshWriteAsyncHandler.write(ctx, msg, promise);
                        final ByteBuf byteBufMsg = (ByteBuf) msg;
//                        byteBufMsg.
                        try {
                            channel.getAsyncIn().writeBuffer(new ByteArrayBuffer()).addListener(future->{

                                if(future.isWritten()){
                                    promise.setSuccess();
                                }else{
                                    promise.setFailure(future.getException());
                                }

                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)  {

                        try {
                            super.disconnect(ctx, promise);
                        }catch (Exception e){}
                    }
                });
//                ch.pipeline().addLast("",new NetconfEOMAggregator());
            }

        });
        p.connect();

//        p.get()
    }

}
