package com.example.managesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String logType;
    private String username;
    private String method;
    private String path;
    private String queryParams;
    private Integer statusCode;
    private Boolean success;
    private String ipAddress;
    private String userAgent;
    private Long durationMs;
    private LocalDateTime createdAt;
}
