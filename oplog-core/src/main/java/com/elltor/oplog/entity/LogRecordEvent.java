package com.elltor.oplog.entity;

import org.springframework.context.ApplicationEvent;

public class LogRecordEvent extends ApplicationEvent {

    public LogRecordEvent(Record source) {
        super(source);
    }

}
