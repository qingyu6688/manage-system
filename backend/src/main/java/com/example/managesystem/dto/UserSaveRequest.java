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
        @NotBlank(message = "昵称不能为空") String nickName,
        Integer gender,
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确") String userPhone,
        @Email(message = "邮箱格式不正确") String userEmail,
        String avatar,
        @Size(min = 8, max = 64, message = "密码长度应为8到64个字符") String password,
        Integer status,
        @NotEmpty(message = "至少选择一个角色") List<Long> roleIds
) {
}
