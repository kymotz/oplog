package com.example.demo.entity;


import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class LogRecordOps{
    private String  key;
    private String  value;
    private boolean isTemplate = true;
    private List<String> functionNames;
    private boolean beforeExecute;

    public LogRecordOps(){

    }

    public LogRecordOps(String key, String value) {
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
        return beforeExecute;
    }

    public void setBeforeExecute(boolean beforeExecute) {
        this.beforeExecute = beforeExecute;
    }

    @Override
    public String toString() {
        return "LogRecordOps{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", isTemplate=" + isTemplate +
                ", functionNames=" + functionNames +
                ", beforeExecute=" + beforeExecute +
                '}';
    }
}
