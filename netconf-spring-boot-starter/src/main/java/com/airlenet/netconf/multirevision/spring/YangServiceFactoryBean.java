package com.airlenet.netconf.multirevision.spring;

import com.airlenet.netconf.spring.NetconfClient;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class YangServiceFactoryBean<T> implements FactoryBean<T> {

    private Class<T> yangServiceInterface;
    private NetconfClient netconfClient;
    private YangServiceMapping yangServiceMapping;

    public YangServiceFactoryBean() {

    }

    public YangServiceFactoryBean(Class<T> yangServiceInterface) {
        this.yangServiceInterface = yangServiceInterface;
    }

    public void setNetconfClient(NetconfClient client) {
        this.netconfClient = client;
    }

    public void setYangServiceMapping(YangServiceMapping yangServiceMapping) {
        this.yangServiceMapping = yangServiceMapping;
    }

    @Override
    public T getObject() throws Exception {
        YangServiceProxy<T> yangServiceProxy = new YangServiceProxy<>(yangServiceInterface);
        yangServiceProxy.setNetconfClient(netconfClient);
        yangServiceProxy.setYangServiceMapping(yangServiceMapping);
        return (T) Proxy.newProxyInstance(this.yangServiceInterface.getClassLoader(), new Class[]{this.yangServiceInterface}, yangServiceProxy);
    }

    @Override
    public Class<?> getObjectType() {
        return yangServiceInterface;
    }
}
