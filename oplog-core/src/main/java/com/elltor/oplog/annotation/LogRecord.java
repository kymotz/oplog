package com.elltor.oplog.annotation;

import java.lang.annotation.*;

/**
 * 日志注解
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecord {

    /** 成功的消息模板 */
    String success();

    /** 失败的消息模板 */
    String fail() default "";

    /** 操作用户 */
    String operator() default "";

    /** 业务编号 */
    String bizNo() default "";

    /** 分类 */
    String category() default "";

    /** 详细/备注 */
    String detail() default "";

    /** 条件情况 */
    String condition() default "";
}