package com.example.managesystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String menuType;
    private String title;
    private String routeName;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Boolean enabled;
    private Boolean hidden;
    private Boolean keepAlive;
    private Boolean hiddenTab;
    private Boolean iframe;
    private String externalLink;
    private Boolean badge;
    private String textBadge;
    private Boolean fixedTab;
    private String activePath;
    private Boolean fullPage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
