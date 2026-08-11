package com.example.managesystem.vo;

import java.time.LocalDateTime;

public record RoleListItem(
        Long roleId,
        String roleName,
        String roleCode,
        String description,
        Boolean enabled,
        LocalDateTime createTime
) {
}
