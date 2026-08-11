package com.example.managesystem.config;

import com.example.managesystem.entity.SysOperationLog;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.OperationLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OperationLogInterceptorTest {

    @Mock
    private OperationLogService operationLogService;

    private OperationLogInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new OperationLogInterceptor(operationLogService);
        LoginUser loginUser = new LoginUser(1L, "admin", "password", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRecordWriteOperationAndMaskSensitiveQuery() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/user/1"
        );
        request.setQueryString("token=secret-value&source=profile");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "unit-test");
        request.setAttribute(OperationLogInterceptor.BUSINESS_SUCCESS_ATTRIBUTE, Boolean.FALSE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        ArgumentCaptor<SysOperationLog> captor = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(operationLogService).record(captor.capture());
        SysOperationLog log = captor.getValue();
        assertEquals("OPERATION", log.getLogType());
        assertEquals("admin", log.getUsername());
        assertEquals("token=***&source=profile", log.getQueryParams());
        assertFalse(log.getSuccess());
    }

    @Test
    void shouldIgnoreReadOperation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(operationLogService, never()).record(any(SysOperationLog.class));
    }

    @Test
    void shouldRecordSuccessfulLoginWithoutAuthenticatedPrincipal() throws Exception {
        SecurityContextHolder.clearContext();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setAttribute(OperationLogInterceptor.LOGIN_USERNAME_ATTRIBUTE, "operator");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        ArgumentCaptor<SysOperationLog> captor = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(operationLogService).record(captor.capture());
        SysOperationLog log = captor.getValue();
        assertEquals("LOGIN", log.getLogType());
        assertEquals("operator", log.getUsername());
        assertEquals("/api/auth/login", log.getPath());
        assertEquals(true, log.getSuccess());
    }

    @Test
    void shouldRecordFailedLoginWithBusinessStatus() throws Exception {
        SecurityContextHolder.clearContext();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setAttribute(OperationLogInterceptor.LOGIN_USERNAME_ATTRIBUTE, "wrong-user");
        request.setAttribute(OperationLogInterceptor.BUSINESS_SUCCESS_ATTRIBUTE, Boolean.FALSE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        ArgumentCaptor<SysOperationLog> captor = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(operationLogService).record(captor.capture());
        SysOperationLog log = captor.getValue();
        assertEquals("LOGIN", log.getLogType());
        assertEquals("wrong-user", log.getUsername());
        assertFalse(log.getSuccess());
        assertEquals(400, log.getStatusCode());
    }
}
