package com.example.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MessageSendRequest(
        @NotBlank(message = "消息标题不能为空")
        @Size(max = 150, message = "消息标题不能超过150个字符")
        String title,
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 5000, message = "消息内容不能超过5000个字符")
        String content,
        @NotBlank(message = "消息类型不能为空")
        @Pattern(regexp = "SYSTEM|REMINDER|TASK", message = "消息类型不正确")
        String type,
        @NotNull(message = "发送范围不能为空")
        Boolean allUsers,
        List<Long> receiverIds
) {
}
