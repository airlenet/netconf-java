package com.airlenet.netconf.multirevision.spring.autoconfig;

import com.airlenet.netconf.multirevision.spring.YangServiceMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YangServiceAutoConfigure {

    @Bean
    YangServiceMapping yangServiceMapping() {
        return new YangServiceMapping();
    }

    @Bean
    YangReferenceAnnotationPostProcessor yangBeanPostProcessor() {
        return new YangReferenceAnnotationPostProcessor();
    }

    @Bean
    static YangServiceBeanRegistry autoConfiguredYangServiceConfigurer() {
        YangServiceBeanRegistry yangServiceBeanRegistry = new YangServiceBeanRegistry();
        return yangServiceBeanRegistry;
    }
}
