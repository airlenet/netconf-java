package com.airlenet.netconf.api;

import com.airlenet.netconf.exception.NetconfException;

public interface NetconfClient {
    NetconfSession getNetconfSession(NetconfDevice netconfDevice) throws NetconfException;

    default NetconfConnect getNetconfConnect(NetconfDevice netconfDevice) throws NetconfException {
        return null;
    }
}
