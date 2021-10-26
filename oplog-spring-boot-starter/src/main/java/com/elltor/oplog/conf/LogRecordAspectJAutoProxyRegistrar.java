package com.elltor.oplog.conf;

import com.elltor.oplog.annotation.EnableLogRecord;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class LogRecordAspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes enableLogRecord = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableLogRecord.class.getName(), false));
        if (enableLogRecord != null) {
            AdviceMode mode = (AdviceMode) enableLogRecord.getOrDefault("mode", AdviceMode.PROXY);
            switch (mode) {
                case PROXY:
                    // 自动根据情况代理，优先JDK代理
                    AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
                    System.out.println("Advice mode : auto proxy");
                    break;
                case ASPECTJ:
                    // cglib 代理
                    AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
                    System.out.println("Advice mode : force aspectj");
            }
        }
    }

}
