package com.example.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AnnouncementStatusRequest(
        @NotBlank(message = "公告状态不能为空")
        @Pattern(regexp = "DRAFT|PUBLISHED", message = "公告状态不正确")
        String status
) {
}
