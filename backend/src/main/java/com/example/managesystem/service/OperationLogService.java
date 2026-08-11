package com.example.managesystem.service;

import com.example.managesystem.entity.SysOperationLog;
import com.example.managesystem.vo.OperationLogListItem;
import com.example.managesystem.vo.PageResponse;

public interface OperationLogService {

    PageResponse<OperationLogListItem> page(
            long current,
            long size,
            String logType,
            String username,
            String path,
            String method,
            Boolean success
    );

    void record(SysOperationLog log);

    void delete(Long id);

    void clear();
}
