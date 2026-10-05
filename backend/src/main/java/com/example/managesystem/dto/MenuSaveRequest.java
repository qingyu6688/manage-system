package com.example.managesystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 长度上限与 sys_menu 字段保持一致，超长内容在参数校验阶段拒绝，避免落库时报 500。
 * 路由名称和权限标识会拼成后端权限串（如 User:add），只允许字母、数字、下划线和短横线。
 */
public record MenuSaveRequest(
        @Min(value = 0, message = "上级节点不正确") Long parentId,
        @NotBlank(message = "节点类型不能为空")
        @Pattern(regexp = "MENU|BUTTON", message = "节点类型必须为菜单或按钮") String menuType,
        @NotBlank(message = "名称不能为空") @Size(max = 100, message = "名称不能超过100个字符") String title,
        @Size(max = 100, message = "路由名称不能超过100个字符")
        @Pattern(regexp = "^$|^[A-Za-z][A-Za-z0-9_-]*$", message = "路由名称只能包含字母、数字、下划线和短横线，且以字母开头")
        String routeName,
        @Size(max = 200, message = "路由地址不能超过200个字符") String path,
        @Size(max = 200, message = "组件路径不能超过200个字符") String component,
        @Size(max = 100, message = "权限标识不能超过100个字符")
        @Pattern(regexp = "^$|^[A-Za-z][A-Za-z0-9_-]*$", message = "权限标识只能包含字母、数字、下划线和短横线，且以字母开头")
        String permission,
        @Size(max = 100, message = "图标不能超过100个字符") String icon,
        Integer sort,
        @NotNull(message = "启用状态不能为空") Boolean enabled,
        Boolean hidden,
        Boolean keepAlive,
        Boolean hiddenTab,
        Boolean iframe,
        @Size(max = 500, message = "外部链接不能超过500个字符")
        @Pattern(regexp = "^$|^https?://.+$", message = "外部链接必须以 http:// 或 https:// 开头")
        String externalLink,
        Boolean badge,
        @Size(max = 30, message = "文本徽章不能超过30个字符") String textBadge,
        Boolean fixedTab,
        @Size(max = 200, message = "激活菜单路径不能超过200个字符") String activePath,
        Boolean fullPage
) {
}
