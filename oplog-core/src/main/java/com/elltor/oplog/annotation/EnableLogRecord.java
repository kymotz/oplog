package com.elltor.oplog.annotation;

import org.springframework.context.annotation.AdviceMode;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableLogRecord {

    /** 租户号 */
    String tenant();

    AdviceMode mode() default AdviceMode.PROXY;

}