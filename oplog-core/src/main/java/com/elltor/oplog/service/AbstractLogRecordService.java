package com.elltor.oplog.service;

import com.elltor.oplog.entity.LogRecordEvent;
import com.elltor.oplog.entity.Record;
import org.springframework.context.ApplicationListener;

/**
 * 日志持久存储服务接口
 */

public abstract class AbstractLogRecordService implements ApplicationListener<LogRecordEvent> {

    @Override
    public void onApplicationEvent(LogRecordEvent event) {
        record((Record) event.getSource());
    }

    /**
     * 持久保存 log
     *
     * @param record 日志实体
     */
    public abstract void record(Record record);

}