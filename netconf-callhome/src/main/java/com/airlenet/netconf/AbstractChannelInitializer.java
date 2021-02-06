package com.airlenet.netconf;

import com.airlenet.netconf.nettyutil.handler.FramingMechanism;
import com.airlenet.netconf.nettyutil.handler.FramingMechanismHandlerFactory;
//import com.airlenet.netconf.nettyutil.handler.NetconfHelloMessageToXMLEncoder;
import io.netty.util.concurrent.Promise;
import io.netty.channel.Channel;


public abstract class AbstractChannelInitializer<S> {

    public static final String NETCONF_MESSAGE_DECODER = "netconfMessageDecoder";
    public static final String NETCONF_MESSAGE_AGGREGATOR = "aggregator";
    public static final String NETCONF_MESSAGE_ENCODER = "netconfMessageEncoder";
    public static final String NETCONF_MESSAGE_FRAME_ENCODER = "frameEncoder";
    public static final String NETCONF_SESSION_NEGOTIATOR = "negotiator";

    public void initialize(Channel ch, Promise<S> promise) {
        ch.pipeline().addLast(NETCONF_MESSAGE_AGGREGATOR, new NetconfEOMAggregator());
        initializeMessageDecoder(ch);
        ch.pipeline().addLast(NETCONF_MESSAGE_FRAME_ENCODER,
                FramingMechanismHandlerFactory.createHandler(FramingMechanism.EOM));
        initializeMessageEncoder(ch);

        initializeSessionNegotiator(ch, promise);
    }

    protected void initializeMessageEncoder(Channel ch) {
        // Special encoding handler for hello message to include additional header if available,
        // it is thrown away after successful negotiation
//        ch.pipeline().addLast(NETCONF_MESSAGE_ENCODER, new NetconfHelloMessageToXMLEncoder());
    }

    protected void initializeMessageDecoder(Channel ch) {
        // Special decoding handler for hello message to parse additional header if available,
        // it is thrown away after successful negotiation
//        ch.pipeline().addLast(NETCONF_MESSAGE_DECODER, new NetconfXMLToHelloMessageDecoder());
    }

    /**
     * Insert session negotiator into the pipeline. It must be inserted after message decoder
     * identified by {@link AbstractChannelInitializer#NETCONF_MESSAGE_DECODER}, (or any other custom decoder processor)
     */
    protected abstract void initializeSessionNegotiator(Channel ch, Promise<S> promise);
}
