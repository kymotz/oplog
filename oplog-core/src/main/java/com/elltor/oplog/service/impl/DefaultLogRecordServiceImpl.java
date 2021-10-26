package com.elltor.oplog.service.impl;

import com.elltor.oplog.entity.Record;
import com.elltor.oplog.service.AbstractLogRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLogRecordServiceImpl extends AbstractLogRecordService {

    private final Logger log = LoggerFactory.getLogger(DefaultLogRecordServiceImpl.class);

    public void record(Record record) {
        log.info(record.toString());
    }

}
