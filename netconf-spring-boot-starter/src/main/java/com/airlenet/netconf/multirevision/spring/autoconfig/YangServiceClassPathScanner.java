package com.airlenet.netconf.multirevision.spring.autoconfig;

import com.airlenet.netconf.multirevision.spring.YangServiceFactoryBean;
import com.airlenet.netconf.multirevision.spring.YangServiceMapping;
import com.airlenet.netconf.multirevision.annotation.YangService;
import com.airlenet.netconf.spring.NetconfClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

public class YangServiceClassPathScanner extends ClassPathBeanDefinitionScanner {
    NetconfClient netconfClient;
    private YangServiceMapping yangServiceMapping;

    public YangServiceClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    private Class<? extends Annotation> annotationClass;

    private YangServiceFactoryBean<?> yangServiceFactoryBean = new YangServiceFactoryBean<Object>();

    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
       return metadata.hasAnnotation(YangService.class.getName());
//        return metadata.isInterface() && metadata.isIndependent() || metadata.isConcrete();
    }

    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            this.logger.warn("No YangService was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            this.processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
           if( ((ScannedGenericBeanDefinition) definition).getMetadata().isInterface()){
               if (logger.isDebugEnabled()) {
                   logger.debug("Creating YangServiceFactoryBean with name '" + holder.getBeanName()
                           + "' and '" + definition.getBeanClassName() + "' YangServiceInterface");
               }

               // the mapper interface is the original class of the bean
               // but, the actual class of the bean is MapperFactoryBean
               definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // issue #59
               definition.setBeanClass(yangServiceFactoryBean.getClass());
               definition.setPrimary(true);
               definition.getPropertyValues().add("netconfClient", new RuntimeBeanReference("netconfClient"));
               definition.getPropertyValues().add("yangServiceMapping", new RuntimeBeanReference("yangServiceMapping"));
           }
        }
    }

    public void setAnnotationClass(Class<? extends Annotation> remoteVersionClass) {
        annotationClass = remoteVersionClass;
    }

    public void registerFilters() {
        boolean acceptAllInterfaces = true;
        if (this.annotationClass != null) {
            this.addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }


        this.addExcludeFilter(new TypeFilter() {
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

}
