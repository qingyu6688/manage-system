package com.example.managesystem.vo;

import java.time.LocalDateTime;

public record OperationLogListItem(
        Long id,
        String logType,
        String username,
        String method,
        String path,
        String queryParams,
        Integer statusCode,
        Boolean success,
        String ipAddress,
        String userAgent,
        Long durationMs,
        LocalDateTime createdAt
) {
}
