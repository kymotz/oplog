package com.elltor.oplog.publisher;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class LogRecordPublisher implements ApplicationEventPublisher {

    @Resource
    ApplicationContext applicationContext;

    @Override
    public void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }

}
