package com.airlenet.netconf.callhome;


import com.airlenet.netconf.NetconfClientSessionNegotiatorFactory;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;
import org.apache.sshd.client.SshClient;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class NetconfCallHomeServerBuilder  {

    private static final long DEFAULT_SESSION_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(5);
    private static final int DEFAULT_CALL_HOME_PORT = 6666;

    private SshClient sshClient;
    private EventLoopGroup nettyGroup;
    private NetconfClientSessionNegotiatorFactory negotiationFactory;
    private InetSocketAddress bindAddress;

    private final CallHomeAuthorizationProvider authProvider;
    private final CallHomeNetconfSubsystemListener subsystemListener;
    private final StatusRecorder recorder;

    public NetconfCallHomeServerBuilder(final CallHomeAuthorizationProvider authProvider,
                                        final CallHomeNetconfSubsystemListener subsystemListener, final StatusRecorder recorder) {
        this.authProvider = authProvider;
        this.subsystemListener = subsystemListener;
        this.recorder = recorder;
    }


    public NetconfCallHomeServer build() {
        CallHomeSessionContext.Factory factory =
                new CallHomeSessionContext.Factory(nettyGroup(), negotiatorFactory(), subsystemListener());
        return new NetconfCallHomeServer(sshClient(), authProvider(), factory, bindAddress(), this.recorder);
    }

    public SshClient getSshClient() {
        return sshClient;
    }

    public void setSshClient(final SshClient sshClient) {
        this.sshClient = sshClient;
    }

    public EventLoopGroup getNettyGroup() {
        return nettyGroup;
    }

    public void setNettyGroup(final EventLoopGroup nettyGroup) {
        this.nettyGroup = nettyGroup;
    }

    public NetconfClientSessionNegotiatorFactory getNegotiationFactory() {
        return negotiationFactory;
    }

    public void setNegotiationFactory(final NetconfClientSessionNegotiatorFactory negotiationFactory) {
        this.negotiationFactory = negotiationFactory;
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    public CallHomeAuthorizationProvider getAuthProvider() {
        return authProvider;
    }

    private InetSocketAddress bindAddress() {
        return bindAddress != null ? bindAddress : defaultBindAddress();
    }

    private EventLoopGroup nettyGroup() {
        return nettyGroup != null ? nettyGroup : defaultNettyGroup();
    }

    private NetconfClientSessionNegotiatorFactory negotiatorFactory() {
        return negotiationFactory != null ? negotiationFactory : defaultNegotiationFactory();
    }

    private CallHomeNetconfSubsystemListener subsystemListener() {
        return subsystemListener;
    }

    private CallHomeAuthorizationProvider authProvider() {
        return authProvider;
    }

    private SshClient sshClient() {
        return sshClient != null ? sshClient : defaultSshClient();
    }

    private static SshClient defaultSshClient() {
        return SshClient.setUpDefaultClient();
    }

    private static NetconfClientSessionNegotiatorFactory defaultNegotiationFactory() {
        return new NetconfClientSessionNegotiatorFactory(new HashedWheelTimer(),
                Optional.empty(), DEFAULT_SESSION_TIMEOUT_MILLIS);
    }

    private static EventLoopGroup defaultNettyGroup() {
        return new DefaultEventLoopGroup();
    }

    private static InetSocketAddress defaultBindAddress() {
        return new InetSocketAddress(DEFAULT_CALL_HOME_PORT);
    }
}
