package com.elltor.oplog.entity;

/**
 * 与注解的字段保持一致
 */
public class Record {
    String success;

    String fail;

    String operator;

    String bizNo;

    String category;

    String detail;

    String condition;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getFail() {
        return fail;
    }

    public void setFail(String fail) {
        this.fail = fail;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

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
