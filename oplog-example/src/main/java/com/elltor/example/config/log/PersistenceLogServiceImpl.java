package com.elltor.example.config.log;

import com.elltor.oplog.entity.Record;
import com.elltor.oplog.service.AbstractLogRecordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 扩展点，持久化存储日志实现类，demo 中所有日志在此输入
 */
@Component
@Slf4j
public class PersistenceLogServiceImpl extends AbstractLogRecordService {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void record(Record record) {
        try {
            log.info("往DB中记录日志 :\n {}", objectMapper.writeValueAsString(record));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
