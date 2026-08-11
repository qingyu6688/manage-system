package com.example.managesystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(min = 2, max = 50, message = "姓名长度应为2到50个字符")
        String realName,
        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 50, message = "昵称长度应为2到50个字符")
        String nickname,
        @NotNull(message = "性别不能为空")
        @Min(value = 1, message = "性别参数不正确")
        @Max(value = 2, message = "性别参数不正确")
        Integer gender,
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过100个字符")
        String email,
        @Size(max = 500, message = "个人简介不能超过500个字符")
        String bio
) {
}
