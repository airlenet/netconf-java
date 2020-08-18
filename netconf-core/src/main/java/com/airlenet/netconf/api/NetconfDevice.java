package com.airlenet.netconf.api;

import com.airlenet.network.NetwokDevice;

public interface NetconfDevice extends NetwokDevice {

    int getConnectTimeout();
}
