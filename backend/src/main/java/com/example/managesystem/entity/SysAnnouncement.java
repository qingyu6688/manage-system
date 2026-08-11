package com.example.managesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_announcement")
public class SysAnnouncement {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String type;
    private String status;
    @TableField("is_top")
    private Boolean top;
    private String publishedBy;
    private LocalDateTime publishedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
