package com.example.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AnnouncementSaveRequest(
        @NotBlank(message = "公告标题不能为空")
        @Size(max = 150, message = "公告标题不能超过150个字符")
        String title,
        @NotBlank(message = "公告内容不能为空")
        @Size(max = 10000, message = "公告内容不能超过10000个字符")
        String content,
        @NotBlank(message = "公告类型不能为空")
        @Pattern(regexp = "SYSTEM|ACTIVITY|REMINDER", message = "公告类型不正确")
        String type,
        @NotBlank(message = "公告状态不能为空")
        @Pattern(regexp = "DRAFT|PUBLISHED", message = "公告状态不正确")
        String status,
        @NotNull(message = "置顶状态不能为空")
        Boolean top
) {
}
