package com.example.demo.service;

import com.example.demo.entity.Record;

public interface ILogRecordService {
    /**
     * 保存 log
     *
     * @param record 日志实体
     */
    void record(Record record);

}