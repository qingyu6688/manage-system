package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.dto.AnnouncementSaveRequest;
import com.example.managesystem.dto.AnnouncementStatusRequest;
import com.example.managesystem.service.AnnouncementService;
import com.example.managesystem.vo.AnnouncementListItem;
import com.example.managesystem.vo.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('R_SUPER', 'R_ADMIN')")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/list")
    public ApiResponse<PageResponse<AnnouncementListItem>> page(
            @RequestParam(defaultValue = "1") @Min(1) long current,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(announcementService.page(current, size, title, type, status));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody AnnouncementSaveRequest request) {
        return ApiResponse.success("公告创建成功", announcementService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementSaveRequest request
    ) {
        announcementService.update(id, request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementStatusRequest request
    ) {
        announcementService.updateStatus(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.success();
    }
}
