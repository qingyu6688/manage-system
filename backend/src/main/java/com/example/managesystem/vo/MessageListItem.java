package com.example.managesystem.vo;

import java.time.LocalDateTime;

public record MessageListItem(
        Long receiverId,
        Long messageId,
        String title,
        String content,
        String type,
        String sender,
        LocalDateTime createdAt,
        Boolean read,
        LocalDateTime readAt
) {
}
