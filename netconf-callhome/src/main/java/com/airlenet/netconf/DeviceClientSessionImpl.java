package com.airlenet.netconf;

import org.apache.sshd.client.ClientFactoryManager;
import org.apache.sshd.client.session.ClientSessionImpl;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.util.buffer.Buffer;

import java.io.IOException;
import java.util.List;

public class DeviceClientSessionImpl extends ClientSessionImpl {
    public DeviceClientSessionImpl(ClientFactoryManager client, IoSession ioSession) throws Exception {
        super(client, ioSession);
    }

    @Override
    protected boolean readIdentification(Buffer buffer) throws IOException {
        return super.readIdentification(buffer);
    }

    @Override
    protected List<String> doReadIdentification(Buffer buffer, boolean server) {
        buffer.rpos(9);
        return super.doReadIdentification(buffer, server);
    }
}
