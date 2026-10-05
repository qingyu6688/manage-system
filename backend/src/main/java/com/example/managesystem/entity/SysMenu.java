package com.example.managesystem.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 可选文本字段在业务层统一规整为 NULL，更新时需要真正写入 NULL：
 * route_name 有唯一索引，空串会让第二个按钮节点冲突。
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String menuType;
    private String title;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String routeName;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String path;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String component;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String permission;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String icon;
    private Integer sort;
    private Boolean enabled;
    private Boolean hidden;
    private Boolean keepAlive;
    private Boolean hiddenTab;
    private Boolean iframe;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String externalLink;
    private Boolean badge;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String textBadge;
    private Boolean fixedTab;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String activePath;
    private Boolean fullPage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
