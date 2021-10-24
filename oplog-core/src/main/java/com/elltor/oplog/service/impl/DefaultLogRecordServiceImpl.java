package com.elltor.oplog.service.impl;

import com.elltor.oplog.entity.Record;
import com.elltor.oplog.service.ILogRecordService;

public class DefaultLogRecordServiceImpl implements ILogRecordService {

    @Override
    public void record(Record record) {
        System.out.println("【logecord】log= " + record);
    }

}
