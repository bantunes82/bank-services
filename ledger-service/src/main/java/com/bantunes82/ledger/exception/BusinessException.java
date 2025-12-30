package com.bantunes82.ledger.exception;

public class BusinessException extends RuntimeException {

    private final transient Object[] params;
    private final ErrorCode errorCode;

    public BusinessException(String message, ErrorCode errorCode, Object... params) {
        super(message);
        this.params = params;
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause, ErrorCode errorCode, Object... params) {
        super(message, cause);
        this.params = params;
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }

    public enum ErrorCode {
        GENERAL,
        ERROR_TO_PERSIST,
        ACCOUNT_NOT_FOUND
    }



}

