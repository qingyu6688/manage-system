package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.dto.MessageSendRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.MessageService;
import com.example.managesystem.vo.MessageListItem;
import com.example.managesystem.vo.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/list")
    public ApiResponse<PageResponse<MessageListItem>> page(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") @Min(1) long current,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String readStatus
    ) {
        return ApiResponse.success(messageService.page(
                loginUser.userId(),
                current,
                size,
                keyword,
                type,
                readStatus
        ));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(messageService.unreadCount(loginUser.userId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('R_SUPER', 'R_ADMIN')")
    public ApiResponse<Long> send(@Valid @RequestBody MessageSendRequest request) {
        return ApiResponse.success("消息发送成功", messageService.send(request));
    }

    @PutMapping("/{receiverId}/read")
    public ApiResponse<Void> markRead(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long receiverId
    ) {
        messageService.markRead(receiverId, loginUser.userId());
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal LoginUser loginUser) {
        messageService.markAllRead(loginUser.userId());
        return ApiResponse.success();
    }
}
