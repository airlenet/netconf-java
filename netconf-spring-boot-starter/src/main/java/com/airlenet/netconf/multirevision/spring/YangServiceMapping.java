package com.airlenet.netconf.multirevision.spring;

import com.airlenet.netconf.multirevision.annotation.YangMethod;
import com.airlenet.netconf.multirevision.annotation.YangService;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YangServiceMapping extends ApplicationObjectSupport implements InitializingBean {
    private final MappingRegistry mappingRegistry = new MappingRegistry();

    @Override
    public void afterPropertiesSet() throws Exception {
        String[] beanNamesForType = this.obtainApplicationContext().getBeanNamesForType(Object.class);
        for (String beanName : beanNamesForType) {
            Class<?> beanType = this.obtainApplicationContext().getType(beanName);
            if (beanType != null && AnnotatedElementUtils.hasAnnotation(beanType, YangService.class)) {
                Object handler = beanName;
                Class<?> handlerType = handler instanceof String ? this.obtainApplicationContext().getType((String) handler) : handler.getClass();
                if (handlerType != null) {
                    Class<?> userType = ClassUtils.getUserClass(handlerType);
                    Map<Method, YangMethodInfo> methods = MethodIntrospector.selectMethods(userType, (MethodIntrospector.MetadataLookup<YangMethodInfo>) (method) -> {
                        try {
                            YangMethod yangMethod = AnnotatedElementUtils.findMergedAnnotation(method, YangMethod.class);
//                            YangCapability[] capabilityList = yangMethod == null ? new YangCapability[0] : yangMethod.capability();
//                            List<YangMethodInfo.YangCapabilityInfo> yangCapabilityInfoList = new ArrayList<>(capabilityList.length);
//                            for (YangCapability yangCapability : capabilityList) {
//                                yangCapabilityInfoList.add(new YangMethodInfo.YangCapabilityInfo(yangCapability));
//                            }
                            return new YangMethodInfo(yangMethod == null ? new Class[0] : yangMethod.moduleClass(),
                                    yangMethod == null ? false : yangMethod.moduleEnable(),
                                    yangMethod == null ? false : yangMethod.ignore(),
                                    yangMethod == null ? false : yangMethod.priority());// this.getMappingForMethod(method, userType);
                        } catch (Throwable throwable) {
                            throw new IllegalStateException("Invalid mapping on handler class [" + userType.getName() + "]: " + method, throwable);
                        }
                    });
                    methods.forEach((method, mapping) -> {
                        if (!mapping.isIgnore()) {
                            Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);

                            this.registerHandlerMethod(handler, invocableMethod, mapping);
                        }
                    });
                }
            }
        }
    }

    protected void registerHandlerMethod(Object handler, Method method, YangMethodInfo mapping) {
        if (1 == method.getModifiers())
            this.mappingRegistry.register(mapping, handler, method);
    }

    public YangHandlerMethod getHandler(Method method, YangMethodInfo.YangCapabilityInfo capability) {
        List<YangMethodInfo> yangMethodInfos = this.mappingRegistry.urlLookup.get(getMethodUniStr(method) + (capability == null ? "" : capability.getCapabilityUri()));
        if (yangMethodInfos == null)
            return null;
        for (YangMethodInfo yangMethodInfo : yangMethodInfos) {
            yangMethodInfo.isPriority();
            return this.mappingRegistry.mappingLookup.get(yangMethodInfo);
        }
        return null;
    }

    public List<YangMethodInfo.YangCapabilityInfo> getYangCapabilityInfo(Method method) {
        List<YangMethodInfo.YangCapabilityInfo> yangCapabilityInfos = this.mappingRegistry.methodLookup.get(getMethodUniStr(method));
        return yangCapabilityInfos;
    }

    public String getMethodUniStr(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> declaringClass = method.getDeclaringClass();
        if(declaringClass.isInterface()){
            sb.append(declaringClass.getTypeName()).append('.');
        }else{
            sb.append(declaringClass.getInterfaces()[0].getTypeName()).append('.');
        }

        sb.append(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> clazz : parameterTypes) {
            sb.append(clazz.getTypeName());
        }
        return sb.toString();
    }

    class MappingRegistry {
        private final Map<YangMethodInfo, YangHandlerMethod> mappingLookup = new LinkedHashMap();
        private final MultiValueMap<String, YangMethodInfo> urlLookup = new LinkedMultiValueMap();
        private final MultiValueMap<String, YangMethodInfo.YangCapabilityInfo> methodLookup = new LinkedMultiValueMap();

        public void register(YangMethodInfo mapping, Object handler, Method method) {
            mappingLookup.put(mapping, createHandlerMethod(handler, method));

            String methodUniStr = getMethodUniStr(method);
            List<YangMethodInfo.YangCapabilityInfo> yangCapabilities = mapping.getYangCapabilities();
            if (yangCapabilities.isEmpty()) {
                urlLookup.add(methodUniStr, mapping);
                methodLookup.addAll(methodUniStr, yangCapabilities);
            } else {
                yangCapabilities.forEach(yangCapabilityInfo -> {
                    urlLookup.add(methodUniStr + yangCapabilityInfo.getCapabilityUri(), mapping);
                    methodLookup.add(methodUniStr, yangCapabilityInfo);

                });
            }
            if (mapping.isModuleEnable()) {
                for (Class<?> moduleClass : mapping.getModuleClass()) {
                    try {
                        Method enableMethod = moduleClass.getDeclaredMethod("enable");
                        enableMethod.invoke(null);
                    } catch (IllegalAccessException e) {
                        logger.warn("Yang Module Class Enable", e);
                    } catch (InvocationTargetException e) {
                        logger.warn("Yang Module Class Enable", e);
                    } catch (NoSuchMethodException e) {
                        logger.warn("Yang Module Class Enable", e);
                    } catch (Exception e) {
                        logger.warn("Yang Module Class Enable", e);
                    }
                }
            }
        }
    }

    protected YangHandlerMethod createHandlerMethod(Object handler, Method method) {
        if (handler instanceof String) {
            return new YangHandlerMethod((String) handler,
                    obtainApplicationContext().getAutowireCapableBeanFactory(), method);
        }
        return new YangHandlerMethod(handler, method);
    }
}
