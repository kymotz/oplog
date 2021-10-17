package com.example.demo.core;

import com.example.demo.annotation.LogRecord;
import com.example.demo.entity.LogRecordOps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LogRecordOperationSource {

    private final static int DEFAULT_OPS_SIZE = 7;

    public List<LogRecordOps> computeLogRecordOperations(Method method, Class<?> targetClass){
        LogRecord logRecord = method.getAnnotation(LogRecord.class);
        List<LogRecordOps> ops = new ArrayList<>(DEFAULT_OPS_SIZE);
//        final Field[] fields = targetClass.getFields();
//        for(Field f : fields){
//            ops.add(new LogRecordOps(f.getName(), f.get))
//        }
        if(!logRecord.success().isEmpty()){
            ops.add(new LogRecordOps("success", logRecord.success()));
        }
        if(!logRecord.bizNo().isEmpty()){
            ops.add(new LogRecordOps("bizNo", logRecord.bizNo()));
        }
        if(!logRecord.operator().isEmpty()){
            ops.add(new LogRecordOps("operator", logRecord.operator()));
        }
        if(!logRecord.category().isEmpty()){
            ops.add(new LogRecordOps("category", logRecord.category()));
        }
        if(!logRecord.detail().isEmpty()){
            ops.add(new LogRecordOps("detail", logRecord.detail()));
        }
        if(!logRecord.condition().isEmpty()){
            ops.add(new LogRecordOps("condition", logRecord.condition()));
        }
        if(!logRecord.fail().isEmpty()){
            ops.add(new LogRecordOps("fail", logRecord.fail()));
        }
        return ops;
    }
}
