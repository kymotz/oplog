package com.example.demo.entity;

public class MethodProceedResult {
    private boolean success;
    private Throwable throwable;
    private String errMsg;

    public MethodProceedResult(boolean success, Throwable exception, String errMsg) {
        this.success = success;
        this.throwable = exception;
        this.errMsg = errMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
