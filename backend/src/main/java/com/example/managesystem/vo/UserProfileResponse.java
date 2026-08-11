package com.example.managesystem.vo;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        Long userId,
        String username,
        String realName,
        String nickname,
        Integer gender,
        String phone,
        String email,
        String avatar,
        String address,
        String bio,
        List<String> roles,
        LocalDateTime createdAt
) {
}
