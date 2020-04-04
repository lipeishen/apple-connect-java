package com.apple.sdk.common.exception;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 3408104373047521559L;
    private int code;
    private String msg;

    public BusinessException(final int code) {
        this.code = code;
    }

    public BusinessException(final int code, final String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public final int getCode() {
        return this.code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
