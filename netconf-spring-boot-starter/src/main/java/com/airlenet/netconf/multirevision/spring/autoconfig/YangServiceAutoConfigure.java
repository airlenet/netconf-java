package com.airlenet.netconf.multirevision.spring.autoconfig;

import com.airlenet.netconf.multirevision.spring.YangServiceMapping;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

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

    @ConditionalOnProperty(
            prefix = "spring.netconf.scan.",
            name = {"base-packages"}
    )
    @Bean
    static YangServiceBeanRegistry autoConfiguredYangServiceConfigurer(@Qualifier("dubbo-service-class-base-packages") Set<String> packagesToScan) {
        YangServiceBeanRegistry yangServiceBeanRegistry = new YangServiceBeanRegistry(packagesToScan);
        return yangServiceBeanRegistry;
    }
}
