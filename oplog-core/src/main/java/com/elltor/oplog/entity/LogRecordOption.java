package com.elltor.oplog.entity;

import java.util.List;

/**
 * 日志选项POJO
 */

public class LogRecordOption {

    private String key;

    private String value;

    private boolean isTemplate = true;

    private List<String> functionNames;

    private boolean isBeforeExecute;

    public LogRecordOption() {

    }

    public LogRecordOption(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        this.isTemplate = template;
    }

    public List<String> getFunctionNames() {
        return functionNames;
    }

    public void setFunctionNames(List<String> functionNames) {
        this.functionNames = functionNames;
    }

    public boolean isBeforeExecute() {
        return isBeforeExecute;
    }

    public void setBeforeExecute(boolean beforeExecute) {
        isBeforeExecute = beforeExecute;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LogRecordOperation{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", isTemplate=").append(isTemplate);
        sb.append(", functionNames=").append(functionNames);
        sb.append(", isBeforeExecute=").append(isBeforeExecute);
        sb.append('}');
        return sb.toString();
    }
}
