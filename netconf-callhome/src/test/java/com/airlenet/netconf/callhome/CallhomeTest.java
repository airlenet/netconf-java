package com.airlenet.netconf.callhome;

import com.airlenet.netconf.callhome.NetconfCallHomeServerBuilder;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.PublicKey;

public class CallhomeTest {

    public static void main(String[] args) throws IOException {
        int port=4334;
        CallHomeAuthorizationProvider provider = new CallHomeAuthProviderImpl();
        CallHomeNetconfSubsystemListener mountDispacher=new CallHomeMountDispatcher();
        StatusRecorder statusReporter=new StatusRecorder() {
            @Override
            public void reportFailedAuth(PublicKey sshKey) {

            }
        };
        NetconfCallHomeServerBuilder builder = new NetconfCallHomeServerBuilder(provider, mountDispacher,
                statusReporter);

        if (port > 0) {
            builder.setBindAddress(new InetSocketAddress(port));
        }
        builder.setNettyGroup(new NioEventLoopGroup());
        NetconfCallHomeServer server = builder.build();
        server.bind();
//        mountDispacher.createTopology();
    }
}
