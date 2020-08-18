package com.airlenet.netconf.callhome;


import com.airlenet.netconf.NetconfClientSession;
import com.airlenet.netconf.NetconfClientSessionListener;
import io.netty.util.concurrent.Promise;

/**
 * Activator of NETCONF channel on incoming SSH Call Home session.
 */
public interface CallHomeChannelActivator {
    /**
     * Activates Netconf Client Channel with supplied client session listener.
     *
     * <p>
     * Activation of channel will result in start of NETCONF client
     * session negotiation on underlying ssh channel.
     *
     * @param listener Client Session Listener to be attached to NETCONF session.
     * @return Promise with negotiated NETCONF session
     */
     Promise<NetconfClientSession> activate(NetconfClientSessionListener listener);
}
