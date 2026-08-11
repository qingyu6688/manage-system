package com.example.managesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_message_receiver")
public class SysMessageReceiver {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long messageId;
    private Long userId;
    private LocalDateTime readAt;
    @TableLogic
    private Integer deleted;
}
