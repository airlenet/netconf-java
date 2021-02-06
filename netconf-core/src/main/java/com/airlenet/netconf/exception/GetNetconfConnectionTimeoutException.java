package com.airlenet.netconf.exception;

public class GetNetconfConnectionTimeoutException extends NetconfException {
    public GetNetconfConnectionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public GetNetconfConnectionTimeoutException(Throwable cause) {
        super(cause);
    }

    public GetNetconfConnectionTimeoutException(String message) {
        super(message);
    }
}
