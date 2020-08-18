package com.airlenet.netconf;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class NetconfEOMAggregator extends DelimiterBasedFrameDecoder {

    public static final ByteBuf DELIMITER = Unpooled.wrappedBuffer("]]>]]>".getBytes());

    public NetconfEOMAggregator() {
        super(Integer.MAX_VALUE, DELIMITER);
    }
}
