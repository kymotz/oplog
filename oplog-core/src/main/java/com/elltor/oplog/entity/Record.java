package com.elltor.oplog.entity;

/**
 * 与注解的字段保持一致
 */
public class Record {

    /** 成功模板 */
    String success;

    /** 失败模板 */
    String fail;

    /** 操作用户 */
    String operator;

    /** 业务编号 */
    String bizNo;

    /** 分类 */
    String category;

    /** 详细信息/备注 */
    String detail;

    /** 条件 */
    String condition;

    /** 是否完成执行 */
    boolean complete;

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

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
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
        sb.append(", complete=").append(complete);
        sb.append('}');
        return sb.toString();
    }
}
