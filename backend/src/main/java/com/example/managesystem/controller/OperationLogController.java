package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.service.OperationLogService;
import com.example.managesystem.vo.OperationLogListItem;
import com.example.managesystem.vo.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('R_SUPER')")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/list")
    public ApiResponse<PageResponse<OperationLogListItem>> page(
            @RequestParam(defaultValue = "1") @Min(1) long current,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size,
            @RequestParam(required = false)
            @Pattern(regexp = "^(OPERATION|LOGIN)$", message = "日志类型不正确") String logType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Boolean success
    ) {
        return ApiResponse.success(
                operationLogService.page(current, size, logType, username, path, method, success)
        );
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        operationLogService.clear();
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        operationLogService.delete(id);
        return ApiResponse.success();
    }
}
