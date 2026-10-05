package com.example.managesystem.service;

import com.example.managesystem.dto.MessageSendRequest;
import com.example.managesystem.vo.MessageListItem;
import com.example.managesystem.vo.PageResponse;

public interface MessageService {
    java.util.List<com.example.managesystem.vo.MessageReceiverOption> receiverOptions(String keyword);

    PageResponse<MessageListItem> page(
            Long userId,
            long current,
            long size,
            String keyword,
            String type,
            String readStatus
    );

    long unreadCount(Long userId);

    Long send(MessageSendRequest request);

    void markRead(Long receiverId, Long userId);

    void markAllRead(Long userId);
}
