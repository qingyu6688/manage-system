package com.example.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.entity.SysOperationLog;
import com.example.managesystem.mapper.SysOperationLogMapper;
import com.example.managesystem.service.OperationLogService;
import com.example.managesystem.vo.OperationLogListItem;
import com.example.managesystem.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final SysOperationLogMapper operationLogMapper;

    public PageResponse<OperationLogListItem> page(
            long current,
            long size,
            String logType,
            String username,
            String path,
            String method,
            Boolean success
    ) {
        long safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .eq(StringUtils.hasText(logType), SysOperationLog::getLogType, logType)
                .like(StringUtils.hasText(username), SysOperationLog::getUsername, username)
                .like(StringUtils.hasText(path), SysOperationLog::getPath, path)
                .eq(StringUtils.hasText(method), SysOperationLog::getMethod, method)
                .eq(success != null, SysOperationLog::getSuccess, success)
                .orderByDesc(SysOperationLog::getCreatedAt);
        IPage<SysOperationLog> page = operationLogMapper.selectPage(
                new Page<>(Math.max(current, 1), safeSize),
                wrapper
        );
        return PageResponse.from(page.convert(this::toListItem));
    }

    public void record(SysOperationLog log) {
        operationLogMapper.insert(log);
    }

    @Transactional
    public void delete(Long id) {
        if (operationLogMapper.deleteById(id) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "操作日志不存在");
        }
    }

    @Transactional
    public void clear() {
        operationLogMapper.deleteAllLogs();
    }

    private OperationLogListItem toListItem(SysOperationLog log) {
        return new OperationLogListItem(
                log.getId(),
                log.getLogType(),
                log.getUsername(),
                log.getMethod(),
                log.getPath(),
                log.getQueryParams(),
                log.getStatusCode(),
                log.getSuccess(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getDurationMs(),
                log.getCreatedAt()
        );
    }
}
