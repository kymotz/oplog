package com.elltor.example.config.log;

import com.elltor.oplog.entity.Record;
import com.elltor.oplog.service.AbstractLogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PresidentLogServiceImpl extends AbstractLogRecordService {

    @Override
    public void record(Record record) {
        log.info("example 包中 : {}", record);
    }
}
