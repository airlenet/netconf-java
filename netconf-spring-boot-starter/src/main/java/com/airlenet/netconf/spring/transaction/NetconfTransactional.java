package com.airlenet.netconf.spring.transaction;

import java.io.Serializable;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface NetconfTransactional {
    String url() default "netconf://127.0.0.1:2020";
    String username();
    String password();
}
