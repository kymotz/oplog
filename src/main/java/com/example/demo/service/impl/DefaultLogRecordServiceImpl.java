package com.example.demo.service.impl;

import com.example.demo.annotation.LogRecord;
import com.example.demo.entity.Record;
import com.example.demo.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLogRecordServiceImpl implements ILogRecordService {

    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Record record) {
        log.info("【logRecord】log={}", record);
    }
}
