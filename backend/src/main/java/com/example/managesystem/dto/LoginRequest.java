package com.example.managesystem.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "请输入用户名") String userName,
        @NotBlank(message = "请输入密码") String password
) {
}
