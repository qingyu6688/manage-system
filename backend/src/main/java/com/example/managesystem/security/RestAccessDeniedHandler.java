package com.example.managesystem.security;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.common.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorDetail errorDetail = ErrorDetail.create(ErrorCode.ACCESS_DENIED, request);
        log.warn(
                "权限拒绝 errorId={} method={} path={}",
                errorDetail.errorId(),
                errorDetail.method(),
                errorDetail.path()
        );
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.failure(
                        ErrorCode.ACCESS_DENIED,
                        ErrorCode.ACCESS_DENIED.defaultMessage(),
                        errorDetail
                )
        );
    }
}
