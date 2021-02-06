package com.airlenet;

import com.airlenet.netconf.client.DefaultNetconfClient;
import com.airlenet.netconf.client.DefaultNetconfDevice;
import com.airlenet.netconf.exception.NetconfException;
import com.airlenet.netconf.api.NetconfClient;
import com.tailf.jnc.*;
import com.tailf.jnc.NetconfSession;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultPromise;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.*;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.session.SessionFactory;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.channel.ChannelListener;
import org.apache.sshd.common.channel.StreamingChannel;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.Readable;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.netty.NettyIoServiceFactory;
import org.apache.sshd.netty.NettyIoServiceFactoryFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntUnaryOperator;

public class NetconfTest {
    public static void main(String[] args) throws JNCException, NetconfException, IOException, InterruptedException {
        EventLoopGroup eventLoopGroup = new DefaultEventLoopGroup();
        SshClient client = SshClient.setUpDefaultClient();
        PropertyResolverUtils.updateProperty(
                client, CoreModuleProperties.SEND_IMMEDIATE_IDENTIFICATION.getName(), false);

        client.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE, Duration.ofSeconds(30));

        client.setSessionFactory(  new SessionFactory(client));
        client.addSessionListener(new SessionListener() {
            @Override
            public void sessionEstablished(Session session) {

            }

            @Override
            public void sessionCreated(Session session) {

            }

            @Override
            public void sessionNegotiationStart(Session session, Map<KexProposalOption, String> clientProposal, Map<KexProposalOption, String> serverProposal) {

            }

            @Override
            public void sessionNegotiationEnd(Session session, Map<KexProposalOption, String> clientProposal, Map<KexProposalOption, String> serverProposal, Map<KexProposalOption, String> negotiatedOptions, Throwable reason) {

            }

            @Override
            public void sessionDisconnect(Session session, int reason, String msg, String language, boolean initiator) {

            }

            @Override
            public void sessionClosed(Session session) {

            }
        });
        client.addChannelListener(new ChannelListener() {
            @Override
            public void channelInitialized(Channel channel) {

            }

            @Override
            public void channelOpenSuccess(Channel channel) {

            }

            @Override
            public void channelOpenFailure(Channel channel, Throwable reason) {

            }

            @Override
            public void channelStateChanged(Channel channel, String hint) {

            }

            @Override
            public void channelClosed(Channel channel, Throwable reason) {

            }
        });
        NettyIoServiceFactoryFactory ioServiceFactoryFactory = new NettyIoServiceFactoryFactory();

        client.setIoServiceFactoryFactory(ioServiceFactoryFactory);

        client.start();
        ClientSession session = client.connect("admin", "172.16.115.72", 2022).verify().getSession();

        session.addPasswordIdentity("admin");
        session.auth().verify();
//        ChannelShell shellChannel = session.createShellChannel();
//        shellChannel.open().verify();
        ChannelSubsystem channelSubsystem = session.createSubsystemChannel("netconf");
        channelSubsystem.setStreaming(StreamingChannel.Streaming.Async);
        channelSubsystem.open().addListener( openFuture->{
            if(openFuture.isOpened()){
                NetconfChannel netconfChannel = new NetconfChannel(channelSubsystem, session);
                eventLoopGroup.register(netconfChannel);
                netconfChannel.pipeline().addLast("aggregator",new NetconfDelimiter());
                netconfChannel.pipeline().addLast("netconfMessageDecoder",new StringDecoder());
                netconfChannel.pipeline().addLast("frameEncoder",new EOMFramingMechanismEncoder());
                netconfChannel.pipeline().addLast("netconfMessageEncoder",new StringEncoder());
                netconfChannel.pipeline().addLast(new SimpleChannelInboundHandler<String>(){

                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                        System.out.println("start===="+s+"=====end");
                    }
                });
                ChannelPromise channelPromise = netconfChannel.newPromise();
                String hello="<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "  <capabilities>\n" +
                        "    <capability>urn:ietf:params:netconf:base:1.0</capability>\n" +
                        "  </capabilities>\n" +
                        "</hello>";

                try {

                    netconfChannel.write(hello,channelPromise).addListener(l->{
                        if(l.isSuccess()){

//                            netconfChannel.write("<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:message-id=\"2\">\n" +
//                                    "  <nc:get>\n" +
//                                    "    <nc:filter nc:type=\"xpath\" nc:select=\"sys-info\"/>  </nc:get>\n" +
//                                    "</nc:rpc>");
//                            netconfChannel.flush();
                        }
                    });
                    netconfChannel.flush();
                    channelPromise.addListener( l->{
                        if(l.isSuccess()){

                            netconfChannel.write("<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:message-id=\"2\">\n" +
                                    "  <nc:get>\n" +
                                    "    <nc:filter nc:type=\"xpath\" nc:select=\"sys-info\"/>  </nc:get>\n" +
                                    "</nc:rpc>");
                            netconfChannel.flush();
                        }
                    });
//                    channelPromise.await();
                    CompletableFuture completableFuture = new CompletableFuture();

//                    completableFuture.complete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                netconfChannel.flush();
            }
        });


//        channelPromise.

//        channelSubsystem.setStreaming(StreamingChannel.Streaming.Sync);
//        OpenFuture open = channelSubsystem.open();
//        open.verify();
//
//
//        channelSubsystem.getInvertedIn().write(hello.getBytes());
//        channelSubsystem.getInvertedIn().flush();
//
//        byte[] bytes =new byte[4096];
//
//        int read = channelSubsystem.getInvertedOut().read(bytes);
////   ]]>]]>
//        System.out.println(new String(bytes,0,read));


        Thread.sleep(1000000);

        client.stop();

    }
}
