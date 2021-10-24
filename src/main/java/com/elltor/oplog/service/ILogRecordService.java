package com.elltor.oplog.service;

import com.elltor.oplog.entity.Record;

public interface ILogRecordService {
    /**
     * 保存 log
     *
     * @param record 日志实体
     */
    void record(Record record);

}