package com.airlenet.netconf.api;

import com.airlenet.netconf.exception.NetconfException;

import java.io.Serializable;

public interface NetconfDevice {

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
     * 访问协议
     * netconf://ssh://username:password@hostname:port
     * netconf://tcp://username:password@hostname:port
     * callhome://ssh://username:password@hostname:port
     *
     * @return netconf callhome
     */
    String getUrl();

    String getHost();

    /**
     * 访问端口
     *
     * @return
     */
    int getPort();

    String getVersion();

    String getZoneId();

    String getUsername();

    String getPassword();

    int getConnectTimeout();

//    default NetconfClient getNetconfClient() throws NetconfException {
//        return null;
//    }
}
