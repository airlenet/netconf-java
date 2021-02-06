package com.airlenet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.io.IOException;
import java.net.SocketAddress;

public class NetconfChannel extends AbstractChannel implements SshFutureListener<IoReadFuture> {
    private static final ChannelMetadata METADATA = new ChannelMetadata(false);
    private final ChannelConfig config = new DefaultChannelConfig(this);
    private final ClientSession session;
    private final ClientChannel sshChannel;
    private static final int BUFFER_SIZE = 2048;
    private   ByteArrayBuffer buf;

    public NetconfChannel(ChannelSubsystem channelSubsystem, ClientSession session) {
        super((Channel) null);
        this.session = session;
        this.sshChannel = channelSubsystem;

        buf = new ByteArrayBuffer(BUFFER_SIZE);
        sshChannel.getAsyncOut().read(buf).addListener(this);
        pipeline().addFirst(createChannelAdapter());

    }

    private ChannelOutboundHandlerAdapter createChannelAdapter() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
                try {
                    sshChannel.getAsyncIn().writeBuffer(toBuffer((ByteBuf) msg)).addListener(ioWriteFuture -> {
                        if(ioWriteFuture.isWritten()){
                            promise.setSuccess();
                        }else{
                            promise.setFailure(ioWriteFuture.getException());
                        }
                    });
                } catch (IOException e) {
                    promise.setFailure(e);
                }
            }
        };
    }

    private static Buffer toBuffer(final ByteBuf msg) {
        // TODO Buffer vs ByteBuf translate, Can we handle that better ?
        msg.resetReaderIndex();
        final byte[] temp = new byte[msg.readableBytes()];
        msg.readBytes(temp, 0, msg.readableBytes());
        return new ByteArrayBuffer(temp);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new SshUnsafe();
    }

    @Override
    protected boolean isCompatible(EventLoop eventLoop) {
        return true;
    }

    @Override
    protected SocketAddress localAddress0() {
        return session.getIoSession().getLocalAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return session.getIoSession().getRemoteAddress();
    }

    @Override
    protected void doBind(SocketAddress socketAddress) throws Exception {

    }

    @Override
    protected void doDisconnect() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {

    }

    @Override
    protected void doBeginRead() throws Exception {

    }

    @Override
    protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception {
        throw new IllegalStateException("Outbound writes to SSH should be done by SSH Write handler");
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return  !session.isClosed() && !session.isClosing();
    }

    @Override
    public boolean isActive() {
        return !session.isClosed() && !session.isClosing();
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    public void operationComplete(IoReadFuture future) {
        if (future.getRead() > 0) {
            final ByteBuf msg = Unpooled.wrappedBuffer(buf.array(), 0, future.getRead());
//            System.out.println(msg.toString(CharsetUtil.UTF_8));
            pipeline().fireChannelRead(msg);
            // Schedule next read
            buf = new ByteArrayBuffer(BUFFER_SIZE);
           sshChannel.getAsyncOut().read(buf).addListener(this);

        }
    }

    private class SshUnsafe extends AbstractUnsafe {
        @Override
        public void connect(final SocketAddress remoteAddress, final SocketAddress localAddress,
                            final ChannelPromise promise) {
            throw new UnsupportedOperationException("Unsafe is not supported.");
        }
    }
}
