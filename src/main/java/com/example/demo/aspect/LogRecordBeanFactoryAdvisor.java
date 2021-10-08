package com.example.demo.aspect;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 *
 */
public class LogRecordBeanFactoryAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    LogRecordPointcut pointcut;

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    void setPointcut(LogRecordPointcut pointcut){
        this.pointcut = pointcut;
    }
}
