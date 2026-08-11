package com.example.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "当前密码不能为空")
        String currentPassword,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 64, message = "新密码长度应为8到64个字符")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "新密码必须同时包含字母和数字")
        String newPassword,
        @NotBlank(message = "确认密码不能为空")
        String confirmPassword
) {
}
