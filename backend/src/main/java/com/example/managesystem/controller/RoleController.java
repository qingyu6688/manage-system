package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.dto.RoleMenuRequest;
import com.example.managesystem.dto.RoleSaveRequest;
import com.example.managesystem.service.RoleService;
import com.example.managesystem.vo.PageResponse;
import com.example.managesystem.vo.RoleListItem;
import com.example.managesystem.vo.RoleOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<PageResponse<RoleListItem>> page(
            @RequestParam(defaultValue = "1") @Min(1) long current,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTime
    ) {
        return ApiResponse.success(roleService.page(
                current,
                size,
                roleName,
                roleCode,
                description,
                enabled,
                startTime,
                endTime
        ));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('R_SUPER') or hasAuthority('User:add') or hasAuthority('User:edit')")
    public ApiResponse<List<RoleOption>> options() {
        return ApiResponse.success(roleService.options());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<Long> create(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success("角色创建成功", roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        roleService.update(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/menu-ids")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<List<Long>> menuIds(@PathVariable Long id) {
        return ApiResponse.success(roleService.getMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<Void> updateMenus(@PathVariable Long id, @Valid @RequestBody RoleMenuRequest request) {
        roleService.updateMenus(id, request.menuIds());
        return ApiResponse.success();
    }
}
