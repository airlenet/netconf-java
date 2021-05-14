package com.airlenet.netconf.datasource.exception;

import com.airlenet.netconf.datasource.NetconfException;

public class NetconfDeviceNotSupportException  extends NetconfException {
    public NetconfDeviceNotSupportException() {
        this("device not support");
    }

    public NetconfDeviceNotSupportException(String message) {
        super(message);
    }

    public NetconfDeviceNotSupportException(Throwable cause) {
        super(cause);
    }

    public NetconfDeviceNotSupportException(String message, Throwable cause) {
        super(message, cause);
    }
}
