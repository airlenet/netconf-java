package com.airlenet.netconf.client;

import com.airlenet.netconf.api.NetconfDevice;

import java.io.File;
import java.io.Serializable;
import java.security.KeyPair;

public class DefaultNetconfDevice implements NetconfDevice {
    private Serializable id;
    private String name;
    private String serialNumber;
    private String username;
    private String ipAddress;
    private String password;
    private int port;
    private int connectTimeout = 10;
    private int replyTimeout = 10;
    private AuthType authType = AuthType.Password;

    public DefaultNetconfDevice(String ipAddress, int port, String username, String password) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.password = password;
        this.port = port;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("device id:").append(id).append("name:").append(name);
        builder.append("netconf://").append(username).append("@").append(ipAddress).append(":").append(port);
        return builder.toString();
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public Serializable id() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    @Override
    public String getIp() {
        return this.ipAddress;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getProtocol() {
        return "netconf";
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public AuthType getAuthType() {
        return authType;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public File getKeyPath() {
        return null;
    }

    @Override
    public KeyPair getKeyPair() {
        return null;
    }
}
