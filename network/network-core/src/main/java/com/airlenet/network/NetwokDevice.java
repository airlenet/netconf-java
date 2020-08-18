package com.airlenet.network;

import java.io.File;
import java.io.Serializable;
import java.security.KeyPair;

public interface NetwokDevice {

    /**
     * 设备Id
     *
     * @return
     */
    Serializable id();

    /**
     * 设备名称
     *
     * @return
     */
    String getName();

    /**
     * 设备唯一标识
     *
     * @return
     */
    String getSerialNumber();

    /**
     * 访问地址
     *
     * @return
     */
    String getIp();

    /**
     * 访问端口
     *
     * @return
     */
    int getPort();


    /**
     * 访问协议
     *
     * @return
     */
    String getProtocol();

    String getUsername();

    AuthType getAuthType();

    String getPassword();

    /**
     * Key 认证文件路径
     *
     * @return
     */
    File getKeyPath();

    KeyPair getKeyPair();

    enum AuthType {
        Password, Key
    }
}
