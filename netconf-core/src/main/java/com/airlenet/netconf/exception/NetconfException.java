package com.airlenet.netconf.exception;

public class NetconfException extends Exception {
    public NetconfException() {
    }

    public NetconfException(String message) {
        super(message);
    }

    public NetconfException(Throwable cause) {
        super(cause);
    }

    public NetconfException(String message, Throwable cause) {
        super(message, cause);
    }
}
