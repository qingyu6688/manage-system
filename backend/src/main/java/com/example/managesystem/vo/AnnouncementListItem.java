package com.example.managesystem.vo;

import java.time.LocalDateTime;

public record AnnouncementListItem(
        Long id,
        String title,
        String content,
        String type,
        String status,
        Boolean top,
        String publishedBy,
        LocalDateTime publishedAt,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt
) {
}
