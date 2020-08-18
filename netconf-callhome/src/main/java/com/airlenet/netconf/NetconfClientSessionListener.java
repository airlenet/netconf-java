package com.airlenet.netconf;

import com.airlenet.netconf.api.NetconfMessage;

public interface NetconfClientSessionListener<S> {
    /**
     * Fired when the session was established successfully.
     *
     * @param session New session
     */
    void onSessionUp(S session);

    /**
     * Fired when the session went down because of an IO error. Implementation should take care of closing underlying
     * session.
     *
     * @param session that went down
     * @param cause Exception that was thrown as the cause of session being down
     */
    void onSessionDown(S session, Exception cause);

    /**
     * Fired when the session is terminated locally. The session has already been closed and transitioned to IDLE state.
     * Any outstanding queued messages were not sent. The user should not attempt to make any use of the session.
     *
     * @param reason the cause why the session went down
     */
//    void onSessionTerminated(S session, NetconfTerminationReason reason);

    /**
     * Fired when a normal protocol message is received.
     *
     * @param message Protocol message
     */
    void onMessage(S session, NetconfMessage message);
}
