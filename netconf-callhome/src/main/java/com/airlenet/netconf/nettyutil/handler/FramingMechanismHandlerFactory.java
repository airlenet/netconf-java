package com.airlenet.netconf.nettyutil.handler;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FramingMechanismHandlerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(FramingMechanismHandlerFactory.class);

    private FramingMechanismHandlerFactory() {
        // not called - private constructor for utility class
    }

    public static MessageToByteEncoder<ByteBuf> createHandler(FramingMechanism framingMechanism) {
        LOG.debug("{} framing mechanism was selected.", framingMechanism);
        if (framingMechanism == FramingMechanism.EOM) {
            return new EOMFramingMechanismEncoder();
        } else {
            return new ChunkedFramingMechanismEncoder();
        }
    }
}
