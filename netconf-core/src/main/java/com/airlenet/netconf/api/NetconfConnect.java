package com.airlenet.netconf.api;

import com.airlenet.netconf.NetconfException;
import com.tailf.jnc.IOSubscriber;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;

import java.io.IOException;

public interface NetconfConnect {
    NetconfSession getNetconfSession(IOSubscriber ioSubscriber) throws NetconfException, IOException, JNCException;
}
