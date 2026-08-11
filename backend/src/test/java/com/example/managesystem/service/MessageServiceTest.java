package com.example.managesystem.service;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.dto.MessageSendRequest;
import com.example.managesystem.entity.SysMessage;
import com.example.managesystem.entity.SysMessageReceiver;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.mapper.SysMessageMapper;
import com.example.managesystem.mapper.SysMessageReceiverMapper;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private SysMessageMapper messageMapper;
    @Mock
    private SysMessageReceiverMapper messageReceiverMapper;
    @Mock
    private SysUserMapper userMapper;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageServiceImpl(messageMapper, messageReceiverMapper, userMapper);
        LoginUser loginUser = new LoginUser(1L, "admin", "password", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSendMessageToDistinctActiveUsers() {
        SysUser first = activeUser(2L);
        SysUser second = activeUser(3L);
        when(userMapper.selectByIds(List.of(2L, 3L))).thenReturn(List.of(first, second));
        doAnswer(invocation -> {
            SysMessage message = invocation.getArgument(0);
            message.setId(20L);
            return 1;
        }).when(messageMapper).insert(any(SysMessage.class));

        Long messageId = messageService.send(new MessageSendRequest(
                " 维护提醒 ",
                " 今晚进行维护。 ",
                "REMINDER",
                false,
                List.of(2L, 2L, 3L)
        ));

        ArgumentCaptor<SysMessage> messageCaptor = ArgumentCaptor.forClass(SysMessage.class);
        ArgumentCaptor<SysMessageReceiver> receiverCaptor =
                ArgumentCaptor.forClass(SysMessageReceiver.class);
        verify(messageMapper).insert(messageCaptor.capture());
        verify(messageReceiverMapper, org.mockito.Mockito.times(2)).insert(receiverCaptor.capture());
        assertEquals(20L, messageId);
        assertEquals("维护提醒", messageCaptor.getValue().getTitle());
        assertEquals("admin", messageCaptor.getValue().getSender());
        assertEquals(List.of(2L, 3L), receiverCaptor.getAllValues().stream()
                .map(SysMessageReceiver::getUserId)
                .toList());
    }

    @Test
    void shouldRejectDisabledReceiver() {
        SysUser disabled = activeUser(2L);
        disabled.setStatus(0);
        when(userMapper.selectByIds(List.of(2L))).thenReturn(List.of(disabled));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> messageService.send(new MessageSendRequest(
                        "通知",
                        "内容",
                        "SYSTEM",
                        false,
                        List.of(2L)
                ))
        );

        assertEquals("所选接收人不存在或已停用", exception.getMessage());
        verify(messageMapper, never()).insert(any(SysMessage.class));
    }

    @Test
    void shouldOnlyMarkOwnedMessageAsRead() {
        when(messageReceiverMapper.selectOwnedReceiver(99L, 1L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> messageService.markRead(99L, 1L)
        );

        assertEquals("消息不存在或不属于当前用户", exception.getMessage());
    }

    @Test
    void shouldMarkUnreadMessageAsRead() {
        SysMessageReceiver receiver = new SysMessageReceiver();
        receiver.setId(9L);
        receiver.setUserId(1L);
        when(messageReceiverMapper.selectOwnedReceiver(9L, 1L)).thenReturn(receiver);

        messageService.markRead(9L, 1L);

        assertNotNull(receiver.getReadAt());
        verify(messageReceiverMapper).updateById(receiver);
    }

    private SysUser activeUser(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(1);
        return user;
    }
}
