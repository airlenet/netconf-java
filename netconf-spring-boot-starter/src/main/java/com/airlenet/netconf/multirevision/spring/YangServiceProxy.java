package com.airlenet.netconf.multirevision.spring;

import com.airlenet.netconf.datasource.NetconfDevice;
import com.airlenet.netconf.datasource.NetconfException;
import com.airlenet.netconf.spring.NetconfClient;
import com.tailf.jnc.Capabilities;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class YangServiceProxy<T> implements InvocationHandler, Serializable {
    private final Class<T> yangServiceInterface;
    private YangServiceMapping yangServiceMapping;
    private NetconfClient netconfClient;

    public YangServiceProxy(Class<T> yangServiceInterface) {
        this.yangServiceInterface = yangServiceInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        NetconfDevice netconfDevice = (NetconfDevice) args[0];
        Capabilities.Capa capa = null;
        String ns = null;
        YangMethodInfo.YangCapabilityInfo capabilityInfo = null;
        List<YangMethodInfo.YangCapabilityInfo> yangCapabilityInfoList = yangServiceMapping.getYangCapabilityInfo(method);
        for (YangMethodInfo.YangCapabilityInfo cap : yangCapabilityInfoList) {
            capa = netconfClient.getCapability(netconfDevice, cap.getNamespace());
            if (capa.getModule().equals(cap.getModule()) && (cap.getRevision() == null ? cap.getRevision().equals("")
                    : capa.getRevision().equals(cap.getRevision()))) {
                capabilityInfo = cap;
                break;
            }
        }
        if (!yangCapabilityInfoList.isEmpty() && capabilityInfo == null) {
            throw new NetconfException("Not Implemented capability for device" + netconfDevice);
        }
        YangHandlerMethod handler = yangServiceMapping.getHandler(method, capabilityInfo);
        if (handler == null) {
            throw new NetconfException("Not exists YangMethod " + method.getName() + " for XXX");
        }
        try {
            return handler.getMethod().invoke(handler.getBean(), args);
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    public void setNetconfClient(NetconfClient netconfClient) {
        this.netconfClient = netconfClient;
    }

    public void setYangServiceMapping(YangServiceMapping yangServiceMapping) {
        this.yangServiceMapping = yangServiceMapping;
    }
}
