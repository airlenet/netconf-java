package com.airlenet.netconf.multirevision.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public class YangHandlerMethod {
    private final Object bean;
    @Nullable
    private final BeanFactory beanFactory;
    private final Class<?> beanType;
    private final Method method;

    public YangHandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.beanFactory = null;
        this.beanType = ClassUtils.getUserClass(bean);
        this.method = method;
    }

    public YangHandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
        this.bean = beanName;
        this.beanFactory = beanFactory;
        Class<?> beanType = beanFactory.getType(beanName);
        if (beanType == null) {
            throw new IllegalStateException("Cannot resolve bean type for bean with name '" + beanName + "'");
        } else {
            this.beanType = ClassUtils.getUserClass(beanType);
            this.method = method;

        }
    }

    public Object getBean() {
        if (this.bean instanceof String)
            return beanFactory.getBean(this.bean.toString());
        return this.bean;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getBeanType() {
        return this.beanType;
    }
}
