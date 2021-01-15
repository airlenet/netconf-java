package com.airlenet.netconf.multirevision.annotation;


import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface YangService {
    String value() default "";
}
