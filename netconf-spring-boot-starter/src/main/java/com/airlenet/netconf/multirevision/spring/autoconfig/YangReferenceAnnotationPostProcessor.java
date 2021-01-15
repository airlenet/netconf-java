package com.airlenet.netconf.multirevision.spring.autoconfig;

import com.airlenet.netconf.multirevision.annotation.YangReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class YangReferenceAnnotationPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements MergedBeanDefinitionPostProcessor, BeanFactoryAware {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap(256);

    private ConfigurableListableBeanFactory beanFactory;

    private AnnotationAttributes findAutowiredAnnotation(AccessibleObject ao) {
        if (ao.getAnnotations().length > 0) {
            AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ao, YangReference.class);
            if (attributes != null) {
                return attributes;
            }
        }
        return null;
    }


    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = this.findAutowiringMetadata(beanName, bean.getClass(), pvs);

        try {
            metadata.inject(bean, beanName, pvs);
            return pvs;
        } catch (BeanCreationException var7) {
            throw var7;
        } catch (Throwable var8) {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", var8);
        }
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        InjectionMetadata metadata = this.findAutowiringMetadata(beanName, beanType, (PropertyValues) null);
        metadata.checkConfigMembers(beanDefinition);
    }

    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
        String cacheKey = StringUtils.hasLength(beanName) ? beanName : clazz.getName();
        InjectionMetadata metadata = (InjectionMetadata) this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = (InjectionMetadata) this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }

                    metadata = this.buildAutowiringMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }

        return metadata;
    }

    private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
        LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList();
        Class targetClass = clazz;

        do {
            LinkedList<InjectionMetadata.InjectedElement> currElements = new LinkedList();
            ReflectionUtils.doWithLocalFields(targetClass, (field) -> {
                AnnotationAttributes ann = this.findAutowiredAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (this.logger.isWarnEnabled()) {
                            this.logger.warn("Autowired annotation is not supported on static fields: " + field);
                        }

                        return;
                    }

                    boolean required = true;
                    currElements.add(new YangReferenceAnnotationPostProcessor.AutowiredFieldElement(field, required));
                }

            });
//            ReflectionUtils.doWithLocalMethods(targetClass, (method) -> {
//                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
//                if (BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
//                    AnnotationAttributes ann = this.findAutowiredAnnotation(bridgedMethod);
//                    if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
//                        if (Modifier.isStatic(method.getModifiers())) {
//                            if (this.logger.isWarnEnabled()) {
//                                this.logger.warn("Autowired annotation is not supported on static methods: " + method);
//                            }
//
//                            return;
//                        }
//
//                        if (method.getParameterCount() == 0 && this.logger.isWarnEnabled()) {
//                            this.logger.warn("Autowired annotation should only be used on methods with parameters: " + method);
//                        }
//
//                        boolean required = true;
//                        PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
//                        currElements.add(new YangReferenceAnnotationPostProcessor.AutowiredMethodElement(method, required, pd));
//                    }
//
//                }
//            });
            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }

    @Nullable
    private Object resolvedCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
        if (cachedArgument instanceof DependencyDescriptor) {
            DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
            Assert.state(this.beanFactory != null, "No BeanFactory available");
            return this.beanFactory.resolveDependency(descriptor, beanName, (Set) null, (TypeConverter) null);
        } else {
            return cachedArgument;
        }
    }

    private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames) {
        if (beanName != null) {
            Iterator var3 = autowiredBeanNames.iterator();

            while (var3.hasNext()) {
                String autowiredBeanName = (String) var3.next();
                if (this.beanFactory != null && this.beanFactory.containsBean(autowiredBeanName)) {
                    this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
                }

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Autowiring by type from bean name '" + beanName + "' to bean named '" + autowiredBeanName + "'");
                }
            }
        }

    }

    private static class ShortcutDependencyDescriptor extends DependencyDescriptor {
        private final String shortcut;
        private final Class<?> requiredType;

        public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut, Class<?> requiredType) {
            super(original);
            this.shortcut = shortcut;
            this.requiredType = requiredType;
        }

        public Object resolveShortcut(BeanFactory beanFactory) {
            return beanFactory.getBean(this.shortcut, this.requiredType);
        }
    }

    private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {
        private final boolean required;
        private volatile boolean cached = false;
        @Nullable
        private volatile Object cachedFieldValue;

        public AutowiredFieldElement(Field field, boolean required) {
            super(field, (PropertyDescriptor) null);
            this.required = required;
        }

        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            Field field = (Field) this.member;
            Object value;
            if (this.cached) {
                value = YangReferenceAnnotationPostProcessor.this.resolvedCachedArgument(beanName, this.cachedFieldValue);
            } else {
                DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
                desc.setContainingClass(bean.getClass());
                Set<String> autowiredBeanNames = new LinkedHashSet(1);
                Assert.state(YangReferenceAnnotationPostProcessor.this.beanFactory != null, "No BeanFactory available");
                TypeConverter typeConverter = YangReferenceAnnotationPostProcessor.this.beanFactory.getTypeConverter();

                try {
                    value = YangReferenceAnnotationPostProcessor.this.beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
                } catch (BeansException var12) {
                    throw new UnsatisfiedDependencyException((String) null, beanName, new InjectionPoint(field), var12);
                }

                synchronized (this) {
                    if (!this.cached) {
                        if (value == null && !this.required) {
                            this.cachedFieldValue = null;
                        } else {
                            this.cachedFieldValue = desc;
                            YangReferenceAnnotationPostProcessor.this.registerDependentBeans(beanName, autowiredBeanNames);
                            if (autowiredBeanNames.size() == 1) {
                                String autowiredBeanName = (String) autowiredBeanNames.iterator().next();
                                if (YangReferenceAnnotationPostProcessor.this.beanFactory.containsBean(autowiredBeanName) && YangReferenceAnnotationPostProcessor.this.beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
                                    this.cachedFieldValue = new YangReferenceAnnotationPostProcessor.ShortcutDependencyDescriptor(desc, autowiredBeanName, field.getType());
                                }
                            }
                        }

                        this.cached = true;
                    }
                }
            }

            if (value != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(bean, value);
            }

        }
    }
}