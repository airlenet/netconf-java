package com.airlenet.netconf.nettyutil.handler;

public enum FramingMechanism {
    /**
     * Chunked framing mechanism.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6242#section-4.2">Chunked
     *      framing mechanism</a>
     */
    CHUNK,
    /**
     * End-of-Message framing mechanism.
     *
     * @see <a
     *      href="http://tools.ietf.org/html/rfc6242#section-4.3">End-of-message
     *      framing mechanism</a>
     */
    EOM
}
