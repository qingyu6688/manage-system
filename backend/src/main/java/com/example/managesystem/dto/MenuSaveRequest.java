package com.example.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuSaveRequest(
        Long parentId,
        @NotBlank(message = "节点类型不能为空") String menuType,
        @NotBlank(message = "名称不能为空") @Size(max = 100) String title,
        String routeName,
        String path,
        String component,
        String permission,
        String icon,
        Integer sort,
        @NotNull(message = "启用状态不能为空") Boolean enabled,
        Boolean hidden,
        Boolean keepAlive,
        Boolean hiddenTab,
        Boolean iframe,
        String externalLink,
        Boolean badge,
        String textBadge,
        Boolean fixedTab,
        String activePath,
        Boolean fullPage
) {
}
