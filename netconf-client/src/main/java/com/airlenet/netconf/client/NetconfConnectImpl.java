package com.airlenet.netconf.client;

import com.airlenet.netconf.NetconfException;
import com.airlenet.netconf.api.NetconfConnect;
import com.airlenet.netconf.api.NetconfDevice;
import com.tailf.jnc.*;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NetconfConnectImpl implements NetconfConnect {

    ClientSession session;
    NetconfDevice netconfDevice;
    ConnectFuture connectFuture;

    public NetconfConnectImpl(NetconfDevice netconfDevice, ConnectFuture connectFuture, ClientSession session) {
        this.netconfDevice = netconfDevice;
        this.connectFuture = connectFuture;
        this.session = session;
    }

    @Override
    public NetconfSession getNetconfSession(IOSubscriber ioSubscriber) throws NetconfException, IOException, JNCException {
        ClientChannel channel = session.createSubsystemChannel("netconf");

        OpenFuture channelFuture = channel.open();
        if (channelFuture.await(netconfDevice.getConnectTimeout(), TimeUnit.SECONDS)) {
            if (channelFuture.isOpened()) {

                SshdSessionChannel sshdSessionChannel = new SshdSessionChannel(session, channel);
                if (ioSubscriber != null) {
                    sshdSessionChannel.addSubscriber(ioSubscriber);
                }
                final YangXMLParser parser = new YangXMLParser();
                final NetconfSession netconfSession = new NetconfSession(sshdSessionChannel, parser);
                return netconfSession;
            } else {
                throw new NetconfException("Failed to open channel with device ");
            }
        } else {
            throw new NetconfException("timeout");
        }
    }

    public boolean isConnected() {
        return this.connectFuture.isConnected();
    }
}
