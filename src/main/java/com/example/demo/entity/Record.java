package com.example.demo.entity;

public class Record {
    String success;

    String fail;

    String operator;

    String bizNo;

    String category;

    String detail;

    String condition;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Record{");
        sb.append("success='").append(success).append('\'');
        sb.append(", fail='").append(fail).append('\'');
        sb.append(", operator='").append(operator).append('\'');
        sb.append(", bizNo='").append(bizNo).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", detail='").append(detail).append('\'');
        sb.append(", condition='").append(condition).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
