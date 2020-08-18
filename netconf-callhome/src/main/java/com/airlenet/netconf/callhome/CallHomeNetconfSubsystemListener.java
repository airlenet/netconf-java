package com.airlenet.netconf.callhome;

/**
 * Listener for successful opening of NETCONF channel on incoming Call Home connections.
 */
public interface CallHomeNetconfSubsystemListener {
    /**
     * Invoked when Netconf Subsystem was successfully opened on incoming SSH Call Home connection.
     *
     * <p>
     * Implementors of this method should use provided {@link CallHomeChannelActivator} to attach
     * {@link NetconfClientSessionListener} to session and to start NETCONF client session negotiation.
     *
     * @param session   Incoming Call Home session on which NETCONF subsystem was successfully opened
     * @param activator Channel Activator to be used in order to start NETCONF Session negotiation.
     */
    void onNetconfSubsystemOpened(CallHomeProtocolSessionContext session, CallHomeChannelActivator activator);
}
