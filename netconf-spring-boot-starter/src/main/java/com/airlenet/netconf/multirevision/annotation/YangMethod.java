package com.airlenet.netconf.multirevision.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface YangMethod {

    @AliasFor("moduleClass")
    Class<?>[] value() default {};

    @AliasFor("value")
    Class<?>[] moduleClass() default {};

    boolean moduleEnable() default true;

    /**
     * 是否忽略此方法
     *
     * @return
     */
    boolean ignore() default false;

    /**
     * 是否优先，如果没有找到，默认优先此方法
     *
     * @return
     */
    boolean priority() default false;

    /**
     * 适配版本号前缀
     *
     * @return
     */
    String versionRegexp() default "";
}
