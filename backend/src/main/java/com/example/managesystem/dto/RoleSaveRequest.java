package com.example.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleSaveRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 50, message = "角色名称不能超过50个字符")
        String roleName,
        @NotBlank(message = "角色编码不能为空")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$", message = "角色编码应为大写字母、数字或下划线")
        String roleCode,
        @Size(max = 200, message = "角色描述不能超过200个字符")
        String description,
        Boolean enabled
) {
}
