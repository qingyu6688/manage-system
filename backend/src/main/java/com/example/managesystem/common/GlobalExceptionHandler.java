package com.example.managesystem.common;

import com.example.managesystem.config.OperationLogInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(exception.getErrorCode(), exception.getMessage(), exception, request, false);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<Void> handleValidationException(
            Exception exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        String message;
        if (exception instanceof MethodArgumentNotValidException validException) {
            message = validException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse(ErrorCode.REQUEST_VALIDATION.defaultMessage());
        } else {
            BindException bindException = (BindException) exception;
            message = bindException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse(ErrorCode.REQUEST_VALIDATION.defaultMessage());
        }
        return failure(ErrorCode.REQUEST_VALIDATION, message, exception, request, false);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse(ErrorCode.REQUEST_VALIDATION.defaultMessage());
        return failure(ErrorCode.REQUEST_VALIDATION, message, exception, request, false);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMediaTypeNotSupportedException.class,
            MultipartException.class
    })
    public ApiResponse<Void> handleMalformedRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(
                ErrorCode.REQUEST_VALIDATION,
                ErrorCode.REQUEST_VALIDATION.defaultMessage(),
                exception,
                request,
                false
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(
                ErrorCode.FILE_UPLOAD_ERROR,
                "上传文件超过大小限制",
                exception,
                request,
                false
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(
                ErrorCode.ACCESS_DENIED,
                ErrorCode.ACCESS_DENIED.defaultMessage(),
                exception,
                request,
                false
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleResourceNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND.defaultMessage(),
                exception,
                request,
                false
        );
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(
                ErrorCode.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED.defaultMessage(),
                exception,
                request,
                false
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SystemException.class)
    public ApiResponse<Void> handleSystemException(
            SystemException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(exception.getErrorCode(), exception.getMessage(), exception, request, true);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataAccessException.class)
    public ApiResponse<Void> handleDataAccessException(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(
                ErrorCode.DATA_ACCESS_ERROR,
                ErrorCode.DATA_ACCESS_ERROR.defaultMessage(),
                exception,
                request,
                true
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknownException(
            Exception exception,
            HttpServletRequest request
    ) {
        markBusinessFailure(request);
        return failure(
                ErrorCode.SYSTEM_ERROR,
                ErrorCode.SYSTEM_ERROR.defaultMessage(),
                exception,
                request,
                true
        );
    }

    private ApiResponse<Void> failure(
            ErrorCode errorCode,
            String message,
            Exception exception,
            HttpServletRequest request,
            boolean includeStackTrace
    ) {
        ErrorDetail errorDetail = ErrorDetail.create(errorCode, request);
        if (includeStackTrace) {
            log.error(
                    "请求处理异常 errorId={} errorCode={} method={} path={} exception={} message={}",
                    errorDetail.errorId(),
                    errorDetail.errorCode(),
                    errorDetail.method(),
                    errorDetail.path(),
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception
            );
        } else {
            log.warn(
                    "请求处理失败 errorId={} errorCode={} method={} path={} exception={} message={}",
                    errorDetail.errorId(),
                    errorDetail.errorCode(),
                    errorDetail.method(),
                    errorDetail.path(),
                    exception.getClass().getSimpleName(),
                    message
            );
        }
        return ApiResponse.failure(errorCode, message, errorDetail);
    }

    private void markBusinessFailure(HttpServletRequest request) {
        request.setAttribute(OperationLogInterceptor.BUSINESS_SUCCESS_ATTRIBUTE, Boolean.FALSE);
    }
}
