package com.airlenet.netconf.client;

import com.airlenet.netconf.NetconfException;
import com.airlenet.netconf.api.NetconfClient;
import com.airlenet.netconf.api.NetconfConnect;
import com.airlenet.netconf.api.NetconfDevice;

import com.google.common.collect.ImmutableSet;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultNetconfClient implements NetconfClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultNetconfClient.class);
    private final SshClient sshClient;
    private long idleTimeout = -1;


    public DefaultNetconfClient(SshClient client) {
        this.sshClient = client == null ? SshClient.setUpDefaultClient() : client;


        if (idleTimeout != -1) {
            sshClient.getProperties().putIfAbsent(FactoryManager.IDLE_TIMEOUT,
                    TimeUnit.SECONDS.toMillis(idleTimeout));
            sshClient.getProperties().putIfAbsent(FactoryManager.NIO2_READ_TIMEOUT,
                    TimeUnit.SECONDS.toMillis(idleTimeout + 15L));
        }
        this.sshClient.start();
    }

    public static NetconfClient setUpDefaultClient() {
        return new DefaultNetconfClient(SshClient.setUpDefaultClient());
    }


    @Override
    public NetconfConnect getNetconfClientConnect(NetconfDevice netconfDevice) throws NetconfException {

        ConnectFuture connectFuture = null;
        try {
            connectFuture = sshClient.connect(netconfDevice.getUsername(),
                    netconfDevice.getIp(),
                    netconfDevice.getPort())
                    .verify(netconfDevice.getConnectTimeout(), TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new NetconfException("Failed to connect." + e.getMessage(), e);
        }
        ClientSession session = connectFuture.getSession();
        if (netconfDevice.getAuthType() == NetconfDevice.AuthType.Key) {
            try (PEMParser pemParser = new PEMParser(new FileReader(netconfDevice.getKeyPath()))) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
                try {
                    KeyPair kp = converter.getKeyPair((PEMKeyPair) pemParser.readObject());
                    session.addPublicKeyIdentity(kp);
                } catch (IOException e) {
                    throw new NetconfException("Failed to authenticate session. Please check if ssk key is generated" +
                            " on   host machine at path " + netconfDevice.getKeyPath() + " : ", e);
                }
            } catch (FileNotFoundException e) {
                throw new NetconfException("Failed to authenticate session. Please check if ssk key is generated" +
                        " on   host machine at path " + netconfDevice.getKeyPath() + " : ", e);
            } catch (IOException e) {
                throw new NetconfException("Failed to authenticate session. Please check if ssk key is generated" +
                        " on   host machine at path " + netconfDevice.getKeyPath() + " : ", e);
            }
        } else {
            session.addPasswordIdentity(netconfDevice.getPassword());
        }

        try {
            session.auth().verify(netconfDevice.getConnectTimeout(), TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new NetconfException("Failed to authenticate session with device",e);
        }
        Set<ClientSession.ClientSessionEvent> event = session.waitFor(
                ImmutableSet.of(ClientSession.ClientSessionEvent.WAIT_AUTH,
                        ClientSession.ClientSessionEvent.CLOSED,
                        ClientSession.ClientSessionEvent.AUTHED), 0);

        if (!event.contains(ClientSession.ClientSessionEvent.AUTHED)) {
            log.debug("Session closed {} {}", event, session.isClosed());
            throw new NetconfException("Failed to authenticate session with device " +
                    "check the user/pwd or key");
        }

        return new NetconfConnectImpl(netconfDevice,connectFuture, session);
    }

    public void stop() {
        if (sshClient != null) {
            this.sshClient.stop();
        }
    }
}
