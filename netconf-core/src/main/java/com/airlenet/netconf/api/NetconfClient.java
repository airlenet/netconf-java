package com.airlenet.netconf.api;

import com.airlenet.netconf.NetconfException;

public interface NetconfClient {
    NetconfConnect getNetconfClientConnect(NetconfDevice netconfDevice) throws NetconfException;
}
