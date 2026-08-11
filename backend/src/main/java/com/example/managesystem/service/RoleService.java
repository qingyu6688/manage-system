package com.example.managesystem.service;

import com.example.managesystem.dto.RoleSaveRequest;
import com.example.managesystem.vo.PageResponse;
import com.example.managesystem.vo.RoleListItem;
import com.example.managesystem.vo.RoleOption;

import java.time.LocalDate;
import java.util.List;

public interface RoleService {

    PageResponse<RoleListItem> page(
            long current,
            long size,
            String roleName,
            String roleCode,
            String description,
            Boolean enabled,
            LocalDate startTime,
            LocalDate endTime
    );

    List<RoleOption> options();

    List<Long> getMenuIds(Long roleId);

    Long create(RoleSaveRequest request);

    void update(Long id, RoleSaveRequest request);

    void delete(Long id);

    void updateMenus(Long roleId, List<Long> menuIds);
}
