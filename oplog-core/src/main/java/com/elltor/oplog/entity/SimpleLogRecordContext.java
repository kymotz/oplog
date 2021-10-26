package com.elltor.oplog.entity;

import com.elltor.oplog.annotation.LogRecord;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.TypedValue;

import java.lang.reflect.Method;

/**
 * SpEL 上下文POJO
 */
public class SimpleLogRecordContext {

    /**
     * SpEL 根对象
     */
    Object rootObject = TypedValue.NULL;

    /**
     * 目标方法入参
     */
    Object[] args;

    /**
     * 目标方法
     */
    Method targetMethod;

    /**
     * 方法标识key，用作缓存
     */
    AnnotatedElementKey methodKey;

    /**
     * 方法上的注解
     */
    LogRecord logRecord;

    /**
     * 方法返回值
     */
    Object ret;

    /**
     * 切面执行过程中的错误信息
     */
    String errMsg;

    public Object getRootObject() {
        return rootObject;
    }

    public void setRootObject(Object rootObject) {
        this.rootObject = rootObject;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public AnnotatedElementKey getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(AnnotatedElementKey methodKey) {
        this.methodKey = methodKey;
    }

    public LogRecord getLogRecord() {
        return logRecord;
    }

    public void setLogRecord(LogRecord logRecord) {
        this.logRecord = logRecord;
    }

    public Object getRet() {
        return ret;
    }

    public void setRet(Object ret) {
        this.ret = ret;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

}
