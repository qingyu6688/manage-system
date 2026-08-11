package com.example.managesystem.config;

import com.example.managesystem.entity.SysOperationLog;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    public static final String BUSINESS_SUCCESS_ATTRIBUTE =
            "com.example.managesystem.operation-log.business-success";
    public static final String LOGIN_USERNAME_ATTRIBUTE =
            "com.example.managesystem.operation-log.login-username";
    private static final String START_TIME_ATTRIBUTE =
            "com.example.managesystem.operation-log.start-time";
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> SENSITIVE_QUERY_KEYS = Set.of(
            "password", "token", "access_token", "refresh_token", "secret", "authorization"
    );

    private final OperationLogService operationLogService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (shouldRecord(request)) {
            request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        Object startedAt = request.getAttribute(START_TIME_ATTRIBUTE);
        if (!(startedAt instanceof Long startTime)) {
            return;
        }

        try {
            boolean loginRequest = isLoginRequest(request);
            String username = resolveUsername(request, loginRequest);
            if (username == null) {
                return;
            }
            Boolean businessSuccess = (Boolean) request.getAttribute(BUSINESS_SUCCESS_ATTRIBUTE);
            boolean success = exception == null
                    && response.getStatus() < HttpServletResponse.SC_BAD_REQUEST
                    && !Boolean.FALSE.equals(businessSuccess);
            int statusCode = response.getStatus();
            if (loginRequest && !success && statusCode < HttpServletResponse.SC_BAD_REQUEST) {
                statusCode = HttpServletResponse.SC_BAD_REQUEST;
            }

            SysOperationLog operationLog = new SysOperationLog();
            operationLog.setLogType(loginRequest ? "LOGIN" : "OPERATION");
            operationLog.setUsername(truncate(username, 30));
            operationLog.setMethod(request.getMethod());
            operationLog.setPath(truncate(request.getRequestURI(), 300));
            operationLog.setQueryParams(sanitizeQueryString(request.getQueryString()));
            operationLog.setStatusCode(statusCode);
            operationLog.setSuccess(success);
            operationLog.setIpAddress(truncate(resolveIpAddress(request), 64));
            operationLog.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
            operationLog.setDurationMs((System.nanoTime() - startTime) / 1_000_000L);
            operationLog.setCreatedAt(LocalDateTime.now());
            operationLogService.record(operationLog);
        } catch (RuntimeException logException) {
            log.warn("操作日志记录失败：{} {}", request.getMethod(), request.getRequestURI(), logException);
        }
    }

    private boolean shouldRecord(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (isLoginRequest(request)) {
            return true;
        }
        return WRITE_METHODS.contains(request.getMethod())
                && path.startsWith("/api/")
                && !path.startsWith("/api/auth/")
                && !path.startsWith("/api/log");
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && "/api/auth/login".equals(request.getRequestURI());
    }

    private String resolveUsername(HttpServletRequest request, boolean loginRequest) {
        if (loginRequest) {
            Object username = request.getAttribute(LOGIN_USERNAME_ATTRIBUTE);
            if (username instanceof String value && !value.isBlank()) {
                return value.trim();
            }
            return "未知账号";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.username();
        }
        return null;
    }

    private String sanitizeQueryString(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return null;
        }
        String sanitized = Arrays.stream(queryString.split("&"))
                .map(this::sanitizeQueryPart)
                .collect(Collectors.joining("&"));
        return truncate(sanitized, 1000);
    }

    private String sanitizeQueryPart(String part) {
        int separatorIndex = part.indexOf('=');
        String key = separatorIndex >= 0 ? part.substring(0, separatorIndex) : part;
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        if (SENSITIVE_QUERY_KEYS.stream().anyMatch(normalizedKey::contains)) {
            return key + "=***";
        }
        return part;
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
