package com.example.managesystem.common;

public enum ErrorCode {
    REQUEST_VALIDATION(400, ErrorType.VALIDATION, "请求参数不正确"),
    FILE_UPLOAD_ERROR(400, ErrorType.VALIDATION, "上传文件不符合要求"),
    AUTHENTICATION_REQUIRED(401, ErrorType.AUTHENTICATION, "登录状态已失效，请重新登录"),
    ACCESS_DENIED(403, ErrorType.AUTHORIZATION, "没有权限执行该操作"),
    RESOURCE_NOT_FOUND(404, ErrorType.RESOURCE, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, ErrorType.VALIDATION, "请求方法不支持"),
    TOO_MANY_LOGIN_ATTEMPTS(429, ErrorType.AUTHENTICATION, "登录失败次数过多，请稍后再试"),
    BUSINESS_ERROR(400, ErrorType.BUSINESS, "业务处理失败"),
    FILE_STORAGE_ERROR(500, ErrorType.SYSTEM, "文件存储处理失败，请稍后重试"),
    DATA_ACCESS_ERROR(500, ErrorType.SYSTEM, "数据访问失败，请稍后重试"),
    SYSTEM_ERROR(500, ErrorType.SYSTEM, "系统繁忙，请稍后重试");

    private final int status;
    private final ErrorType type;
    private final String defaultMessage;

    ErrorCode(int status, ErrorType type, String defaultMessage) {
        this.status = status;
        this.type = type;
        this.defaultMessage = defaultMessage;
    }

    public int status() {
        return status;
    }

    public ErrorType type() {
        return type;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public static ErrorCode fromStatus(int status) {
        return switch (status) {
            case 401 -> AUTHENTICATION_REQUIRED;
            case 403 -> ACCESS_DENIED;
            case 404 -> RESOURCE_NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 500 -> SYSTEM_ERROR;
            default -> BUSINESS_ERROR;
        };
    }
}
