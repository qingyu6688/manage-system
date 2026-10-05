package com.example.managesystem.service;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.dto.AnnouncementSaveRequest;
import com.example.managesystem.dto.AnnouncementStatusRequest;
import com.example.managesystem.entity.SysAnnouncement;
import com.example.managesystem.mapper.SysAnnouncementMapper;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.impl.AnnouncementServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private SysAnnouncementMapper announcementMapper;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser(1L, "admin", "password", true,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("R_SUPER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreatePublishedAnnouncement() {
        doAnswer(invocation -> {
            SysAnnouncement announcement = invocation.getArgument(0);
            announcement.setId(10L);
            return 1;
        }).when(announcementMapper).insert(any(SysAnnouncement.class));

        Long id = announcementService.create(new AnnouncementSaveRequest(
                "系统维护通知",
                "系统将在周六进行维护。",
                "SYSTEM",
                "PUBLISHED",
                true
        ));

        assertEquals(10L, id);
        verify(announcementMapper).insert(any(SysAnnouncement.class));
    }

    @Test
    void shouldClearPublishInfoWhenRevertedToDraft() {
        SysAnnouncement announcement = new SysAnnouncement();
        announcement.setId(1L);
        announcement.setStatus("PUBLISHED");
        announcement.setPublishedBy("admin");
        announcement.setPublishedAt(java.time.LocalDateTime.now());
        when(announcementMapper.selectById(1L)).thenReturn(announcement);

        announcementService.updateStatus(1L, new AnnouncementStatusRequest("DRAFT"));

        assertEquals("DRAFT", announcement.getStatus());
        assertNull(announcement.getPublishedBy());
        assertNull(announcement.getPublishedAt());
        assertNotNull(announcement.getUpdatedAt());
        verify(announcementMapper).updateById(announcement);
    }

    @Test
    void shouldRejectMissingAnnouncement() {
        when(announcementMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> announcementService.delete(99L)
        );

        assertEquals("公告不存在", exception.getMessage());
    }
}
