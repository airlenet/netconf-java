package com.airlenet.netconf.callhome;

import com.airlenet.netconf.AsyncSshHandlerReader;
import com.airlenet.netconf.AsyncSshHandlerWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.Closeable;

import java.net.SocketAddress;

import static java.util.Objects.requireNonNull;

public class MinaSshNettyChannel  extends AbstractServerChannel {
    private static final ChannelMetadata METADATA = new ChannelMetadata(false);

    private final ChannelConfig config = new DefaultChannelConfig(this);
    private final CallHomeSessionContext context;
    private final ClientSession session;
    private final ClientChannel sshChannel;
    private final AsyncSshHandlerReader sshReadHandler;
    private final AsyncSshHandlerWriter sshWriteAsyncHandler;

    private volatile boolean nettyClosed = false;
    MinaSshNettyChannel(final CallHomeSessionContext context, final ClientSession session,
                        final ClientChannel sshChannel) {
        this.context = requireNonNull(context);
        this.session = requireNonNull(session);
        this.sshChannel = requireNonNull(sshChannel);
        this.sshReadHandler = new AsyncSshHandlerReader(
                new ConnectionClosedDuringRead(), new FireReadMessage(), "netconf", sshChannel.getAsyncOut());
        this.sshWriteAsyncHandler = new AsyncSshHandlerWriter(sshChannel.getAsyncIn());
        pipeline().addFirst(createChannelAdapter());
    }
    private ChannelOutboundHandlerAdapter createChannelAdapter() {
        return new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
                sshWriteAsyncHandler.write(ctx, msg, promise);
            }
        };
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    private static boolean notClosing(final Closeable sshCloseable) {
        return !sshCloseable.isClosing() && !sshCloseable.isClosed();
    }

    @Override
    public boolean isOpen() {
        return notClosing(session);
    }

    @Override
    public boolean isActive() {
        return notClosing(session);
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new SshUnsafe();
    }

    @Override
    protected boolean isCompatible(final EventLoop loop) {
        return true;
    }

    @Override
    protected SocketAddress localAddress0() {
        return session.getIoSession().getLocalAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return context.getRemoteAddress();
    }

    @Override
    protected void doBind(final SocketAddress localAddress) {
        throw new UnsupportedOperationException("Bind not supported.");
    }

    void doMinaDisconnect(final boolean blocking) {
        if (notClosing(session)) {
            sshChannel.close(blocking);
            session.close(blocking);
        }
    }

    void doNettyDisconnect() {
        if (!nettyClosed) {
            nettyClosed = true;
            pipeline().fireChannelInactive();
            sshReadHandler.close();
            sshWriteAsyncHandler.close();
        }
    }

    @Override
    protected void doDisconnect() {
//        LOG.info("Disconnect invoked");
        doNettyDisconnect();
        doMinaDisconnect(false);
    }

    @Override
    protected void doClose() {
        context.removeSelf();
        if (notClosing(session)) {
            session.close(true);
            sshChannel.close(true);
        }
    }

    @Override
    protected void doBeginRead() {
        // Intentional NOOP - read is started by AsyncSshHandlerReader
    }

    @Override
    protected void doWrite(final ChannelOutboundBuffer in) {
        throw new IllegalStateException("Outbound writes to SSH should be done by SSH Write handler");
    }

    private final class FireReadMessage implements AsyncSshHandlerReader.ReadMsgHandler {
        @Override
        public void onMessageRead(final ByteBuf msg) {
            pipeline().fireChannelRead(msg);
        }
    }

    private final class ConnectionClosedDuringRead implements AutoCloseable {

        /**
         * Invoked when SSH session dropped during read using {@link AsyncSshHandlerReader}.
         */
        @Override
        public void close() {
            doNettyDisconnect();
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
