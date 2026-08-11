package com.example.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.dto.MessageSendRequest;
import com.example.managesystem.entity.SysMessage;
import com.example.managesystem.entity.SysMessageReceiver;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.mapper.SysMessageMapper;
import com.example.managesystem.mapper.SysMessageReceiverMapper;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.security.SecurityUtils;
import com.example.managesystem.service.MessageService;
import com.example.managesystem.vo.MessageListItem;
import com.example.managesystem.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SysMessageMapper messageMapper;
    private final SysMessageReceiverMapper messageReceiverMapper;
    private final SysUserMapper userMapper;

    public PageResponse<MessageListItem> page(
            Long userId,
            long current,
            long size,
            String keyword,
            String type,
            String readStatus
    ) {
        long safeSize = Math.min(Math.max(size, 1), 100);
        IPage<MessageListItem> page = messageReceiverMapper.selectMessagePage(
                new Page<>(Math.max(current, 1), safeSize),
                userId,
                keyword,
                type,
                readStatus
        );
        return PageResponse.from(page);
    }

    public long unreadCount(Long userId) {
        return messageReceiverMapper.selectUnreadCount(userId);
    }

    @Transactional
    public Long send(MessageSendRequest request) {
        List<Long> receiverIds = resolveReceiverIds(request);
        if (receiverIds.isEmpty()) {
            throw new BusinessException("没有可接收消息的启用用户");
        }

        LocalDateTime now = LocalDateTime.now();
        SysMessage message = new SysMessage();
        message.setTitle(request.title().trim());
        message.setContent(request.content().trim());
        message.setType(request.type());
        message.setSender(SecurityUtils.currentUsername());
        message.setCreatedAt(now);
        messageMapper.insert(message);

        receiverIds.forEach(userId -> {
            SysMessageReceiver receiver = new SysMessageReceiver();
            receiver.setMessageId(message.getId());
            receiver.setUserId(userId);
            messageReceiverMapper.insert(receiver);
        });
        return message.getId();
    }

    @Transactional
    public void markRead(Long receiverId, Long userId) {
        SysMessageReceiver receiver = messageReceiverMapper.selectOwnedReceiver(receiverId, userId);
        if (receiver == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "消息不存在或不属于当前用户"
            );
        }
        if (receiver.getReadAt() == null) {
            receiver.setReadAt(LocalDateTime.now());
            messageReceiverMapper.updateById(receiver);
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        messageReceiverMapper.markAllRead(userId);
    }

    private List<Long> resolveReceiverIds(MessageSendRequest request) {
        if (Boolean.TRUE.equals(request.allUsers())) {
            return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getStatus, 1))
                    .stream()
                    .map(SysUser::getId)
                    .toList();
        }

        if (request.receiverIds() == null || request.receiverIds().isEmpty()) {
            throw new BusinessException("请选择至少一个接收人");
        }
        List<Long> distinctIds = request.receiverIds().stream().distinct().toList();
        List<SysUser> users = userMapper.selectByIds(distinctIds).stream()
                .filter(user -> Integer.valueOf(1).equals(user.getStatus()))
                .toList();
        if (users.size() != distinctIds.size()) {
            throw new BusinessException("所选接收人不存在或已停用");
        }
        return distinctIds;
    }
}
