package com.airlenet.netconf.nettyutil.handler;

import com.airlenet.netconf.api.NetconfHelloMessage;
import com.airlenet.netconf.api.NetconfMessage;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class NetconfHelloMessageToXMLEncoder extends NetconfMessageToXMLEncoder {
    @Override
    @VisibleForTesting
    public void encode(ChannelHandlerContext ctx, NetconfMessage msg, ByteBuf out)
            throws IOException, TransformerException {
        Preconditions.checkState(msg instanceof NetconfHelloMessage, "Netconf message of type %s expected, was %s",
                NetconfHelloMessage.class, msg.getClass());
//        Optional<NetconfHelloMessageAdditionalHeader> headerOptional = ((NetconfHelloMessage) msg)
//                .getAdditionalHeader();
//
//        // If additional header present, serialize it along with netconf hello message
//        if (headerOptional.isPresent()) {
//            out.writeBytes(headerOptional.get().toFormattedString().getBytes(StandardCharsets.UTF_8));
//        }

        super.encode(ctx, msg, out);
    }
}
