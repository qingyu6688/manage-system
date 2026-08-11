package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.dto.UserSaveRequest;
import com.example.managesystem.service.UserService;
import com.example.managesystem.vo.PageResponse;
import com.example.managesystem.vo.UserListItem;
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
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('R_SUPER', 'R_ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public ApiResponse<PageResponse<UserListItem>> page(
            @RequestParam(defaultValue = "1") @Min(1) long current,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userGender,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(userService.page(
                current,
                size,
                userName,
                userGender,
                userPhone,
                userEmail,
                status
        ));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success("用户创建成功", userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveRequest request) {
        userService.update(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success();
    }
}
