package com.airlenet.netconf.nettyutil.handler;

import com.airlenet.netconf.api.NetconfMessage;
import com.google.common.annotations.VisibleForTesting;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;

import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class NetconfMessageToXMLEncoder extends MessageToByteEncoder<NetconfMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(NetconfMessageToXMLEncoder.class);

    private final @Nullable
    String clientId;

    public NetconfMessageToXMLEncoder() {
        this(Optional.empty());
    }

    public NetconfMessageToXMLEncoder(final Optional<String> clientId) {
        this.clientId = clientId.orElse(null);
    }

    @Override
    @VisibleForTesting
    public void encode(final ChannelHandlerContext ctx, final NetconfMessage msg, final ByteBuf out)
            throws IOException, TransformerException {
        LOG.trace("Sent to encode : {}", msg);

        if (clientId != null) {
            Comment comment = msg.getDocument().createComment("clientId:" + clientId);
            msg.getDocument().appendChild(comment);
        }

        try (OutputStream os = new ByteBufOutputStream(out)) {
            // Wrap OutputStreamWriter with BufferedWriter as suggested in javadoc for OutputStreamWriter

            // Using custom BufferedWriter that does not provide newLine method as performance improvement
            // see javadoc for BufferedWriter
            StreamResult result =
                    new StreamResult(new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8)));
            DOMSource source = new DOMSource(msg.getDocument());
            ThreadLocalTransformers.getPrettyTransformer().transform(source, result);
        }
    }
}
