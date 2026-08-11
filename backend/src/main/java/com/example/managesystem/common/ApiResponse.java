package com.example.managesystem.common;

public record ApiResponse<T>(int code, String msg, T data, ErrorDetail error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(200, "操作成功", null, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, null);
    }

    public static ApiResponse<Void> failure(int code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }

    public static ApiResponse<Void> failure(
            ErrorCode errorCode,
            String message,
            ErrorDetail errorDetail
    ) {
        return new ApiResponse<>(errorCode.status(), message, null, errorDetail);
    }
}
