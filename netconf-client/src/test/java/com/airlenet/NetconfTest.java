package com.airlenet;

import com.airlenet.netconf.client.DefaultNetconfClient;
import com.airlenet.netconf.client.DefaultNetconfDevice;
import com.airlenet.netconf.NetconfException;
import com.airlenet.netconf.api.NetconfClient;
import com.tailf.jnc.*;
import com.tailf.jnc.NetconfSession;
import org.apache.sshd.client.SshClient;

import java.io.IOException;

public class NetconfTest {
    public static void main(String[] args) throws JNCException, NetconfException, IOException {
        SshClient client = SshClient.setUpDefaultClient();
        DefaultNetconfDevice defaultNetconfDevice = new DefaultNetconfDevice("172.19.106.72", 2022,"admin", "admin");

        NetconfClient netconfClient = DefaultNetconfClient.setUpDefaultClient();
        NetconfSession netconfSession = netconfClient.getNetconfClientConnect(defaultNetconfDevice).getNetconfSession(new DefaultIOSubscriber("11"));

        netconfSession.get("sys-info");


        DefaultNetconfDevice defaultNetconfDevice1 = new DefaultNetconfDevice("172.19.104.188", 2022,"admin","admin");

        NetconfSession netconfSession1 = netconfClient.getNetconfClientConnect(defaultNetconfDevice1).getNetconfSession(new DefaultIOSubscriber("11"));

        netconfSession1.get("sys-info");

//        NetconfSession session = defaultNetconfDevice.newSession(new DefaultIOSubscriber("11"));
//        session.get("sys-info");
//        session.getCapabilities();
//

//        Device device = new Device("", new DeviceUser("admin", "admin", "admin"), "172.19.106.72", 2022);
//        device.connect("admin");
//        device.newSession("sss");
//        device.getSession("sss").get("sys-info");
    }
}
