package com.example.managesystem.common;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public record ErrorDetail(
        String errorId,
        String errorCode,
        String errorType,
        String method,
        String path,
        LocalDateTime timestamp
) {

    public static ErrorDetail create(ErrorCode errorCode, HttpServletRequest request) {
        String errorId = "ERR-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
        return new ErrorDetail(
                errorId,
                errorCode.name(),
                errorCode.type().name(),
                request.getMethod(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }
}
