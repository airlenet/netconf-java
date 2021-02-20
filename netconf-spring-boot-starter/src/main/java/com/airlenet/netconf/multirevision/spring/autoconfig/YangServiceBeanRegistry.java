package com.airlenet.netconf.multirevision.spring.autoconfig;

import com.airlenet.netconf.multirevision.annotation.YangService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

public class YangServiceBeanRegistry implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {
    private BeanFactory beanFactory;
    private ResourceLoader resourceLoader;
    private String beanName;
    private ApplicationContext applicationContext;

    protected final Set<String> packagesToScan;
    public YangServiceBeanRegistry(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {

        YangServiceClassPathScanner scanner = new YangServiceClassPathScanner(beanDefinitionRegistry);

        try {
            if (this.resourceLoader != null) {
                scanner.setResourceLoader(this.resourceLoader);
            }
            List<String> packages = AutoConfigurationPackages.get(this.applicationContext);
            packagesToScan.addAll(packages);
            scanner.setBeanNameGenerator(new YangServiceNameGenerator());
            scanner.setAnnotationClass(YangService.class);
            scanner.registerFilters();
            scanner.doScan(StringUtils.toStringArray(packagesToScan));
        } catch (IllegalStateException e) {
            throw e;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}