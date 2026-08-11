package com.example.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.dto.AnnouncementSaveRequest;
import com.example.managesystem.dto.AnnouncementStatusRequest;
import com.example.managesystem.entity.SysAnnouncement;
import com.example.managesystem.mapper.SysAnnouncementMapper;
import com.example.managesystem.security.SecurityUtils;
import com.example.managesystem.service.AnnouncementService;
import com.example.managesystem.vo.AnnouncementListItem;
import com.example.managesystem.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final String PUBLISHED = "PUBLISHED";
    private static final String DRAFT = "DRAFT";

    private final SysAnnouncementMapper announcementMapper;

    public PageResponse<AnnouncementListItem> page(
            long current,
            long size,
            String title,
            String type,
            String status
    ) {
        long safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<SysAnnouncement>()
                .like(StringUtils.hasText(title), SysAnnouncement::getTitle, title)
                .eq(StringUtils.hasText(type), SysAnnouncement::getType, type)
                .eq(StringUtils.hasText(status), SysAnnouncement::getStatus, status)
                .orderByDesc(SysAnnouncement::getTop)
                .orderByDesc(SysAnnouncement::getPublishedAt)
                .orderByDesc(SysAnnouncement::getCreatedAt);
        IPage<SysAnnouncement> page = announcementMapper.selectPage(
                new Page<>(Math.max(current, 1), safeSize),
                wrapper
        );
        return PageResponse.from(page.convert(this::toListItem));
    }

    @Transactional
    public Long create(AnnouncementSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String username = SecurityUtils.currentUsername();
        SysAnnouncement announcement = new SysAnnouncement();
        applyRequest(announcement, request);
        applyPublication(announcement, request.status(), username, now);
        announcement.setCreatedBy(username);
        announcement.setUpdatedBy(username);
        announcement.setCreatedAt(now);
        announcement.setUpdatedAt(now);
        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Transactional
    public void update(Long id, AnnouncementSaveRequest request) {
        SysAnnouncement announcement = requireAnnouncement(id);
        LocalDateTime now = LocalDateTime.now();
        String username = SecurityUtils.currentUsername();
        applyRequest(announcement, request);
        applyPublication(announcement, request.status(), username, now);
        announcement.setUpdatedBy(username);
        announcement.setUpdatedAt(now);
        announcementMapper.updateById(announcement);
    }

    @Transactional
    public void updateStatus(Long id, AnnouncementStatusRequest request) {
        SysAnnouncement announcement = requireAnnouncement(id);
        LocalDateTime now = LocalDateTime.now();
        String username = SecurityUtils.currentUsername();
        applyPublication(announcement, request.status(), username, now);
        announcement.setUpdatedBy(username);
        announcement.setUpdatedAt(now);
        announcementMapper.updateById(announcement);
    }

    @Transactional
    public void delete(Long id) {
        requireAnnouncement(id);
        announcementMapper.deleteById(id);
    }

    private void applyRequest(SysAnnouncement announcement, AnnouncementSaveRequest request) {
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setType(request.type());
        announcement.setTop(request.top());
    }

    private void applyPublication(
            SysAnnouncement announcement,
            String status,
            String username,
            LocalDateTime now
    ) {
        announcement.setStatus(status);
        if (PUBLISHED.equals(status)) {
            announcement.setPublishedBy(username);
            announcement.setPublishedAt(now);
            return;
        }
        if (DRAFT.equals(status)) {
            announcement.setPublishedBy(null);
            announcement.setPublishedAt(null);
        }
    }

    private SysAnnouncement requireAnnouncement(Long id) {
        SysAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公告不存在");
        }
        return announcement;
    }

    private AnnouncementListItem toListItem(SysAnnouncement announcement) {
        return new AnnouncementListItem(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getType(),
                announcement.getStatus(),
                announcement.getTop(),
                announcement.getPublishedBy(),
                announcement.getPublishedAt(),
                announcement.getCreatedBy(),
                announcement.getCreatedAt(),
                announcement.getUpdatedBy(),
                announcement.getUpdatedAt()
        );
    }
}
