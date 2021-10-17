package com.example.demo.core;

import com.example.demo.annotation.LogRecord;
import com.example.demo.entity.LogRecordOps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LogRecordOperationSource {

    private final static int DEFAULT_OPS_SIZE = 7;

    private final String templatePrefix = LogRecordExpressionEvaluator.TEMPLATE_PARSER_CONTEXT.getExpressionPrefix();


    public List<LogRecordOps> computeLogRecordOperations(Method method, Class<?> targetClass){
        LogRecord logRecord = method.getAnnotation(LogRecord.class);
        List<LogRecordOps> ops = new ArrayList<>(DEFAULT_OPS_SIZE);
        if(!logRecord.success().isEmpty()){
            LogRecordOps op = new LogRecordOps("success", logRecord.success());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.bizNo().isEmpty()){
            LogRecordOps op = new LogRecordOps("bizNo", logRecord.bizNo());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.operator().isEmpty()){
            LogRecordOps op = new LogRecordOps("operator", logRecord.operator());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.category().isEmpty()){
            LogRecordOps op = new LogRecordOps("category", logRecord.category());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.detail().isEmpty()){
            LogRecordOps op = new LogRecordOps("detail", logRecord.detail());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.condition().isEmpty()){
            LogRecordOps op = new LogRecordOps("condition", logRecord.condition());
            fillTemplated(op);
            ops.add(op);
        }
        if(!logRecord.fail().isEmpty()){
            LogRecordOps op = new LogRecordOps("fail", logRecord.fail());
            fillTemplated(op);
            ops.add(op);
        }
        return ops;
    }

    private void fillTemplated(LogRecordOps op){
        String opValue = op.getValue();
        op.setTemplated(opValue.contains(templatePrefix));
    }
}
