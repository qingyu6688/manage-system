package com.example.managesystem.common;

public class SystemException extends ApplicationException {

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, errorCode.defaultMessage(), cause);
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
