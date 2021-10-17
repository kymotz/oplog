package com.example.demo.entity;


import java.util.StringJoiner;

public class LogRecordOps {
    private String  key;
    private String  value;
    private boolean templated = true;

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

    public boolean isTemplated() {
        return templated;
    }

    public void setTemplated(boolean templated) {
        this.templated = templated;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LogRecordOps.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("value='" + value + "'")
                .add("templated=" + templated)
                .toString();
    }
}
