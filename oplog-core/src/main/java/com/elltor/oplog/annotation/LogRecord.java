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

    /**
     * 成功的消息模板
     */
    String success();

    /**
     * 失败的消息模板
     */
    String fail() default "";

    /**
     * 操作用户
     */
    String operator() default "";

    /**
     * 业务编号
     */
    String bizNo() default "";

    /**
     * 日志类别
     */
    String category() default "";

    /**
     * 详细/备注
     */
    String detail() default "";

    /**
     * 是否记录日志的条件，字符串boolean值，只能为true/false或TRUE/FALSE <p>
     *
     * eg ：
     * <pre>
     * condition = "true"
     * condition = "false"
     * condition = "{#func()}"  // func 是自定义函数, 返回 true/false 字符串
     * condition = "{#func(#user.userid)}" // 根据用户的情况判读是否需要记录日志，eg. 仅记录特定角色用户的操作行为
     * </pre>
     */
    String condition() default "true";

}