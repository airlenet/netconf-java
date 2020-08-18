package com.airlenet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import org.apache.sshd.client.SshClient;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

public class NetconfSessionPromise<S extends NetconfSession> extends DefaultPromise<S> {
    private InetSocketAddress address;
    private final Bootstrap bootstrap;
    EventExecutor executor;
    private Future<?> pending;

    public NetconfSessionPromise(EventExecutor executor, InetSocketAddress address, SshClient sshClient, Bootstrap b) {
        bootstrap =b;
        this.address = requireNonNull(address);
        this.executor= executor;
    }

    synchronized void connect() {

        if (this.address.isUnresolved()) {
            this.address = new InetSocketAddress(this.address.getHostName(), this.address.getPort());
        }

        final ChannelFuture connectFuture = this.bootstrap.connect(this.address);


        // Add listener that attempts reconnect by invoking this method again.
        connectFuture.addListener(new BootstrapConnectListener());

        this.pending = connectFuture;
    }

    private class BootstrapConnectListener implements ChannelFutureListener {

        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {

            NetconfSessionPromise.this.pending =executor.newSucceededFuture(null);
        }
    }
}
