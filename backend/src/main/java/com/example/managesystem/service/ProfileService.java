package com.example.managesystem.service;

import com.example.managesystem.dto.PasswordChangeRequest;
import com.example.managesystem.dto.ProfileUpdateRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.vo.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    UserProfileResponse currentProfile(LoginUser loginUser);

    UserProfileResponse updateProfile(LoginUser loginUser, ProfileUpdateRequest request);

    UserProfileResponse uploadAvatar(LoginUser loginUser, MultipartFile file);

    void changePassword(LoginUser loginUser, PasswordChangeRequest request);
}
