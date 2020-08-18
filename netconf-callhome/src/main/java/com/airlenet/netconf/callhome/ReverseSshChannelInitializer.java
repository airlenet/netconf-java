package com.airlenet.netconf.callhome;

import com.airlenet.netconf.AbstractChannelInitializer;
import com.airlenet.netconf.NetconfClientSession;
import com.airlenet.netconf.NetconfClientSessionListener;
import com.airlenet.netconf.NetconfClientSessionNegotiatorFactory;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;

public class ReverseSshChannelInitializer extends AbstractChannelInitializer<NetconfClientSession> {


    public ReverseSshChannelInitializer(NetconfClientSessionNegotiatorFactory negotiatorFactory, NetconfClientSessionListener listener) {

    }

    public static ReverseSshChannelInitializer create(NetconfClientSessionNegotiatorFactory negotiatorFactory,
                                                      NetconfClientSessionListener listener) {
        return new ReverseSshChannelInitializer(negotiatorFactory, listener);
    }

    @Override
    protected void initializeSessionNegotiator(Channel ch, Promise<NetconfClientSession> promise) {

//        ch.pipeline().addAfter(NETCONF_MESSAGE_DECODER, AbstractChannelInitializer.NETCONF_SESSION_NEGOTIATOR,
//                negotiatorFactory.getSessionNegotiator(this, ch, promise));
    }
}
