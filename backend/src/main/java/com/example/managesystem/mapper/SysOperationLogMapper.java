package com.example.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.managesystem.entity.SysOperationLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    @Delete("DELETE FROM sys_operation_log")
    int deleteAllLogs();
}
