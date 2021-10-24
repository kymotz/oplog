package com.elltor.oplog.service.impl;

import com.elltor.oplog.entity.Record;
import com.elltor.oplog.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLogRecordServiceImpl implements ILogRecordService {

    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Record record) {
        log.info("【logRecord】log={}", record);
    }
}
