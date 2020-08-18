package com.airlenet.netconf.callhome;

import java.security.PublicKey;

public interface StatusRecorder {
    void reportFailedAuth(PublicKey sshKey);
}
