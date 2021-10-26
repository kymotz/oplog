package com.elltor.oplog.annotation;

import com.elltor.oplog.conf.LogRecordAspectJAutoProxyRegistrar;
import com.elltor.oplog.conf.LogRecordProxyAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({LogRecordAspectJAutoProxyRegistrar.class, LogRecordProxyAutoConfiguration.class})
public @interface EnableLogRecord {

    /**
     * 租户号, 应用标识
     */
    String tenant();

    /**
     * 代理模式：
     *  PROXY：自动根据类的情况进行代理，优先JDK代理
     *  ASPECTJ：强制Cglib代理
     */
    AdviceMode mode() default AdviceMode.PROXY;

}