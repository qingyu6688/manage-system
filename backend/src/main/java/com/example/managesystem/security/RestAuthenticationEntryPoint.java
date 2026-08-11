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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorDetail errorDetail = ErrorDetail.create(ErrorCode.AUTHENTICATION_REQUIRED, request);
        log.warn(
                "认证失败 errorId={} method={} path={}",
                errorDetail.errorId(),
                errorDetail.method(),
                errorDetail.path()
        );
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.failure(
                        ErrorCode.AUTHENTICATION_REQUIRED,
                        ErrorCode.AUTHENTICATION_REQUIRED.defaultMessage(),
                        errorDetail
                )
        );
    }
}
