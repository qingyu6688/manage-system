package com.example.managesystem.vo;

import java.time.LocalDateTime;

public record FileListItem(
        Long id,
        String originalName,
        String contentType,
        Long fileSize,
        String uploader,
        LocalDateTime createdAt
) {
}
