package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.dto.MenuSaveRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.MenuService;
import com.example.managesystem.vo.MenuRoute;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/v3/system/menus")
    public ApiResponse<List<MenuRoute>> currentUserMenus(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(menuService.currentUserMenus(loginUser));
    }

    @GetMapping("/menu/tree")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<List<MenuRoute>> allMenus() {
        return ApiResponse.success(menuService.allMenus());
    }

    @PostMapping("/menu")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<Long> create(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success("菜单创建成功", menuService.create(request));
    }

    @PutMapping("/menu/{id}")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        menuService.update(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/menu/{id}")
    @PreAuthorize("hasAuthority('R_SUPER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.success();
    }
}
