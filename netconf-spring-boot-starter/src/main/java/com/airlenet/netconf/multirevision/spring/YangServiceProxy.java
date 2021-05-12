package com.airlenet.netconf.multirevision.spring;

import com.airlenet.netconf.datasource.NetconfDevice;
import com.airlenet.netconf.datasource.NetconfException;
import com.airlenet.netconf.spring.NetconfClient;
import com.tailf.jnc.Capabilities;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        YangMethodInfo.YangCapabilityInfo capabilityInfo = null;
        List<YangMethodInfo.YangCapabilityInfo> yangCapabilityInfoList = yangServiceMapping.getYangCapabilityInfo(method);
        String version = (String) netconfDevice.getExtraInfo("version");
        Capabilities capabilities = netconfClient.getCapabilities(netconfDevice);
        List<Capabilities.Capa> dataCapas = capabilities.getDataCapas();
        for (Capabilities.Capa curCapa : dataCapas) {
            for (YangMethodInfo.YangCapabilityInfo yangCapabilityInfo : yangCapabilityInfoList) {

                if (Objects.equals(yangCapabilityInfo.getNamespace(), curCapa.getUri())
                        && Objects.equals(yangCapabilityInfo.getModule(), curCapa.getModule())
                        && Objects.equals(yangCapabilityInfo.getRevision(), curCapa.getRevision())
                ) {
                    if (StringUtils.hasText(version) && StringUtils.hasText(yangCapabilityInfo.getVersionRegexp())
                            && Pattern.compile(yangCapabilityInfo.getVersionRegexp()).matcher(version).matches()) {
                        capabilityInfo = yangCapabilityInfo;
                        break;
                    } else {
                        capabilityInfo = yangCapabilityInfo;
                        break;
                    }
                }
            }
        }

        if (!yangCapabilityInfoList.isEmpty() && capabilityInfo == null) {
            throw new NetconfException("Not Implemented capability @YangMethod" + method.toString());
        }
        YangHandlerMethod handler = yangServiceMapping.getHandler(method, capabilityInfo, version);
        if (handler == null) {
            throw new NetconfException("Not Implemented capability @YangMethod " + method.toString() + " for capability: module=" + capabilityInfo.getModule() + " ns=" + capabilityInfo.getNamespace() + " revision=" + capabilityInfo.getRevision());
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
