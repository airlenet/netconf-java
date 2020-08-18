package com.airlenet.netconf.callhome;

import java.net.SocketAddress;
import java.security.PublicKey;

/**
 * Provider responsible for resolving CallHomeAuthorization.
 */
public interface CallHomeAuthorizationProvider {
    /**
     * Provides authorization parameters for incoming call-home connection.
     *
     * @param remoteAddress Remote socket address of incoming connection
     * @param serverKey     SSH key provided by SSH server on incoming connection
     * @return {@link CallHomeAuthorization} with authorization information.
     */
    CallHomeAuthorization provideAuth( SocketAddress remoteAddress, PublicKey serverKey);
}
