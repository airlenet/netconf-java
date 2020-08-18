package com.airlenet.netconf;

import org.apache.sshd.client.ClientFactoryManager;
import org.apache.sshd.client.session.ClientSessionImpl;
import org.apache.sshd.client.session.SessionFactory;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.util.Readable;

public class DeviceSessionFactory extends SessionFactory {
    public DeviceSessionFactory(ClientFactoryManager client) {
        super(client);
    }

    @Override
    public void messageReceived(IoSession ioSession, Readable message) throws Exception {
        message.available();
        byte[] x=new byte[9];
        message.getRawBytes(x,0,9);
        super.messageReceived(ioSession, message);
    }

    @Override
    protected ClientSessionImpl doCreateSession(IoSession ioSession) throws Exception {
        return new DeviceClientSessionImpl(this.getClient(), ioSession);
    }
}
