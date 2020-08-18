package com.airlenet.netconf.callhome;

import java.net.SocketAddress;
import java.security.PublicKey;

public interface CallHomeProtocolSessionContext {

    /**
     * Returns session identifier provided by  CallHomeAuthorizationProvider.
     *
     * @return Returns application-provided session identifier
     */
    String getSessionId();

    /**
     * Returns public key provided by remote SSH Server for this session.
     *
     * @return public key provided by remote SSH Server
     */
    PublicKey getRemoteServerKey();

    /**
     * Returns remote socket address associated with this session.
     *
     * @return remote socket address associated with this session.
     */
    SocketAddress getRemoteAddress();

    /**
     * Terminate this session.
     */
    void terminate();

    /**
     * Returns transport type for this session.
     *
     * @return {@link TransportType} for this session.
     */
    TransportType getTransportType();
}
