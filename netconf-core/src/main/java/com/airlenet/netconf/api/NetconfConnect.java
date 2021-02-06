package com.airlenet.netconf.api;

import com.airlenet.netconf.exception.NetconfException;
import com.tailf.jnc.JNCException;

import java.io.IOException;

public interface NetconfConnect {
    NetconfSession getNetconfSession() throws NetconfException, IOException;
}
