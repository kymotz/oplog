package com.elltor.example.entity;

import java.io.Serializable;

public class Result implements Serializable {

    private String msg;

    private Integer status;

    private Object data;

    public Result() {

    }

    public Result(String msg, Integer status, Object data) {
        this.msg = msg;
        this.status = status;
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
