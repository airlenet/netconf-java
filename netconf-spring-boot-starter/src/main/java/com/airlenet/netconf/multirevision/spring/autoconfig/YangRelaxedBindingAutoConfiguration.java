package com.airlenet.netconf.multirevision.spring.autoconfig;

import com.airlenet.netconf.multirevision.spring.PropertySourcesUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@ConditionalOnClass(
        name = {"org.springframework.boot.context.properties.bind.Binder"}
)
@Configuration
public class YangRelaxedBindingAutoConfiguration {
    public PropertyResolver dubboScanBasePackagesPropertyResolver(final ConfigurableEnvironment environment) {
        ConfigurableEnvironment propertyResolver = new AbstractEnvironment() {
            protected void customizePropertySources(MutablePropertySources propertySources) {
                Map<String, Object> dubboScanProperties = PropertySourcesUtils.getSubProperties(environment.getPropertySources(), "spring.netconf.scan.");
                propertySources.addLast(new MapPropertySource("yangScanProperties", dubboScanProperties));
            }
        };
        ConfigurationPropertySources.attach(propertyResolver);
        return propertyResolver;
    }

    @ConditionalOnMissingBean(
            name = {"dubbo-service-class-base-packages"}
    )
    @Bean(
            name = {"dubbo-service-class-base-packages"}
    )
    public Set<String> dubboBasePackages(ConfigurableEnvironment environment) {
        PropertyResolver propertyResolver = this.dubboScanBasePackagesPropertyResolver(environment);
        return (Set)propertyResolver.getProperty("base-packages", Set.class, Collections.emptySet());
    }
}
