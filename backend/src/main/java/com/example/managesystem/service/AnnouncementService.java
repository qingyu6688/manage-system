package com.example.managesystem.service;

import com.example.managesystem.dto.AnnouncementSaveRequest;
import com.example.managesystem.dto.AnnouncementStatusRequest;
import com.example.managesystem.vo.AnnouncementListItem;
import com.example.managesystem.vo.PageResponse;

public interface AnnouncementService {

    PageResponse<AnnouncementListItem> page(
            long current,
            long size,
            String title,
            String type,
            String status
    );

    Long create(AnnouncementSaveRequest request);

    void update(Long id, AnnouncementSaveRequest request);

    void updateStatus(Long id, AnnouncementStatusRequest request);

    void delete(Long id);
}
