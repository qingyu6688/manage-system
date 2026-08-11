package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.dto.PasswordChangeRequest;
import com.example.managesystem.dto.ProfileUpdateRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.AvatarStorageService;
import com.example.managesystem.service.ProfileService;
import com.example.managesystem.vo.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final AvatarStorageService avatarStorageService;

    @GetMapping
    public ApiResponse<UserProfileResponse> currentProfile(
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return ApiResponse.success(profileService.currentProfile(loginUser));
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return ApiResponse.success("个人资料保存成功", profileService.updateProfile(loginUser, request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileResponse> uploadAvatar(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success("头像上传成功", profileService.uploadAvatar(loginUser, file));
    }

    @GetMapping("/avatar/{fileName:.+}")
    public ResponseEntity<Resource> avatar(@PathVariable String fileName) {
        AvatarStorageService.AvatarFile avatar = avatarStorageService.load(fileName);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .contentType(MediaType.parseMediaType(avatar.contentType()))
                .body(avatar.resource());
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        profileService.changePassword(loginUser, request);
        return ApiResponse.success("密码修改成功，请重新登录", null);
    }
}
