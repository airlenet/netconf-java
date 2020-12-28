package com.airlenet.netconf.spring.transaction;

import com.airlenet.netconf.datasource.NetconfDataSource;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class NetconfTransactionAdvisor extends StaticMethodMatcherPointcutAdvisor {
    protected NetconfDataSource multiNetconfDataSource;
    NetconfTransactionterceptor transactionterceptor;

    public NetconfTransactionAdvisor() {
        setAdvice(transactionterceptor = new NetconfTransactionterceptor());
    }

    public void setMultiNetconfDataSource(NetconfDataSource multiNetconfDataSource) {
        transactionterceptor.setNetconfDataSource(multiNetconfDataSource);
        this.multiNetconfDataSource = multiNetconfDataSource;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        Method m = method;
        if (this.isAuthzAnnotationPresent(method)) {
            return true;
        } else {
            if (targetClass != null) {
                try {
                    m = targetClass.getMethod(m.getName(), m.getParameterTypes());
                    return this.isAuthzAnnotationPresent(m) || this.isAuthzAnnotationPresent(targetClass);
                } catch (NoSuchMethodException var5) {
                }
            }

            return false;
        }
    }

    private boolean isAuthzAnnotationPresent(Class<?> targetClazz) {
        Annotation a = AnnotationUtils.findAnnotation(targetClazz, NetconfTransactional.class);
        if (a != null) {
            return true;
        }

        return false;
    }

    private boolean isAuthzAnnotationPresent(Method method) {

        Annotation a = AnnotationUtils.findAnnotation(method, NetconfTransactional.class);
        if (a != null) {
            return true;
        }


        return false;
    }
}
