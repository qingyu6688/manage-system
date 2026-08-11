package com.example.managesystem.common;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private static final String BUSINESS_SUCCESS_ATTRIBUTE =
            "com.example.managesystem.operation-log.business-success";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnTraceableBusinessError() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/user/999");

        ApiResponse<Void> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"),
                request
        );

        assertEquals(404, response.code());
        assertEquals("用户不存在", response.msg());
        assertNull(response.data());
        assertNotNull(response.error());
        assertTrue(response.error().errorId().startsWith("ERR-"));
        assertEquals("RESOURCE_NOT_FOUND", response.error().errorCode());
        assertEquals("RESOURCE", response.error().errorType());
        assertEquals("DELETE", response.error().method());
        assertEquals("/api/user/999", response.error().path());
        assertEquals(Boolean.FALSE, request.getAttribute(BUSINESS_SUCCESS_ATTRIBUTE));
    }

    @Test
    void shouldClassifyFileStorageErrorAndHideCauseMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/file");
        SystemException exception = new SystemException(
                ErrorCode.FILE_STORAGE_ERROR,
                new IOException("磁盘绝对路径和内部故障信息")
        );

        ApiResponse<Void> response = handler.handleSystemException(exception, request);

        assertEquals(500, response.code());
        assertEquals(ErrorCode.FILE_STORAGE_ERROR.defaultMessage(), response.msg());
        assertFalse(response.msg().contains("磁盘绝对路径"));
        assertEquals("FILE_STORAGE_ERROR", response.error().errorCode());
        assertEquals("SYSTEM", response.error().errorType());
    }

    @Test
    void shouldClassifyDatabaseError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/list");

        ApiResponse<Void> response = handler.handleDataAccessException(
                new DataAccessResourceFailureException("database unavailable"),
                request
        );

        assertEquals(500, response.code());
        assertEquals("DATA_ACCESS_ERROR", response.error().errorCode());
        assertEquals(ErrorCode.DATA_ACCESS_ERROR.defaultMessage(), response.msg());
    }

    @Test
    void shouldKeepSuccessResponseWithoutErrorDetail() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertEquals(200, response.code());
        assertEquals("ok", response.data());
        assertNull(response.error());
    }
}
