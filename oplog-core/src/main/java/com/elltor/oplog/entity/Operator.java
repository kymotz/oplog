package com.elltor.oplog.entity;


import java.io.Serializable;

/**
 * 操作日志的操作用户
 */

public class Operator implements Serializable {

    private String name;

    private String username;

    public Operator() {

    }

    public Operator(String name, String username) {
        this.name = name;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Operator{");
        sb.append("name='").append(name).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
