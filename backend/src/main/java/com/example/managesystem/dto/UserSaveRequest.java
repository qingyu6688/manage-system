package com.example.managesystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserSaveRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 30, message = "用户名长度应为2到30个字符")
        String userName,
        @NotBlank(message = "昵称不能为空") @Size(max = 50, message = "昵称不能超过50个字符") String nickName,
        @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(2) Integer gender,
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确") String userPhone,
        @Email(message = "邮箱格式不正确") @Size(max = 100, message = "邮箱不能超过100个字符") String userEmail,
        @Size(max = 500, message = "头像地址过长") String avatar,
        // 与个人中心修改密码的规则保持一致，避免管理员设置出更弱的初始密码。
        @Pattern(regexp = "(?s)^$|^(?=.*[A-Za-z])(?=.*\\d).{8,64}$", message = "密码应为8到64个字符，且同时包含字母和数字")
        String password,
        @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(2) Integer status,
        @NotEmpty(message = "至少选择一个角色") List<@jakarta.validation.constraints.NotNull Long> roleIds
) {
}
