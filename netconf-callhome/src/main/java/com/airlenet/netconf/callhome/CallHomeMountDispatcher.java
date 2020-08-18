package com.airlenet.netconf.callhome;

import com.airlenet.netconf.NetconfClientSessionListener;
import com.airlenet.netconf.api.NetconfMessage;

public class CallHomeMountDispatcher implements CallHomeNetconfSubsystemListener{
    @Override
    public void onNetconfSubsystemOpened(CallHomeProtocolSessionContext session, CallHomeChannelActivator activator) {
//        final CallHomeMountSessionContext deviceContext =
//                getSessionManager().createSession(session, activator, onCloseHandler);
//        if (deviceContext != null) {
//            final NodeId nodeId = deviceContext.getId();
//            final Node configNode = deviceContext.getConfigNode();
//            LOG.info("Provisioning fake config {}", configNode);
//            topology.connectNode(nodeId, configNode);
//        }
        activator.activate(new NetconfClientSessionListener() {
            @Override
            public void onSessionUp(Object session) {

            }

            @Override
            public void onSessionDown(Object session, Exception cause) {

            }

            @Override
            public void onMessage(Object session, NetconfMessage message) {

            }
        });
//        activator.activate(new NetconfClientSessionListener());
    }
}
