package com.airlenet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class NetconfDelimiter extends DelimiterBasedFrameDecoder {

    public static final ByteBuf DELIMITER = Unpooled.wrappedBuffer("]]>]]>".getBytes());

    public NetconfDelimiter() {
        super(Integer.MAX_VALUE, DELIMITER);
    }
}
