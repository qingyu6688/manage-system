package com.example.managesystem.common;

public class BusinessException extends ApplicationException {

    public BusinessException(int code, String message) {
        super(ErrorCode.fromStatus(code), message);
    }

    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
