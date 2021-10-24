package com.elltor.oplog.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecord {
    String success();

    String fail() default "";

    String operator() default "";

    String bizNo();

    String category() default "";

    String detail() default "";

    String condition() default "";
}