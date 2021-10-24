package com.elltor.oplog.service;

import com.elltor.oplog.entity.Record;

/**
 * 日志持久存储服务接口
 */

public interface ILogRecordService {

    /**
     * 持久保存 log
     *
     * @param record 日志实体
     */
    void record(Record record);

}