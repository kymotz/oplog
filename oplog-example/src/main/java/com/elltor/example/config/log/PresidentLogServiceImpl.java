package com.elltor.example.config.log;

import com.elltor.oplog.entity.Record;
import com.elltor.oplog.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PresidentLogServiceImpl implements ILogRecordService {

    @Override
    public void record(Record record) {
        log.info("oplog example receive log : {}", record);
    }
}
