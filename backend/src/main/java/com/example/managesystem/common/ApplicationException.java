package com.example.managesystem.common;

import lombok.Getter;

@Getter
public abstract class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    protected ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getCode() {
        return errorCode.status();
    }
}
