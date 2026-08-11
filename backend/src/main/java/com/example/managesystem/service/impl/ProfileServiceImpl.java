package com.example.managesystem.service.impl;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.dto.PasswordChangeRequest;
import com.example.managesystem.dto.ProfileUpdateRequest;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.AvatarStorageService;
import com.example.managesystem.service.ProfileService;
import com.example.managesystem.vo.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AvatarStorageService avatarStorageService;

    public UserProfileResponse currentProfile(LoginUser loginUser) {
        return toResponse(requireActiveUser(loginUser.userId()));
    }

    @Transactional
    public UserProfileResponse updateProfile(LoginUser loginUser, ProfileUpdateRequest request) {
        SysUser user = requireActiveUser(loginUser.userId());
        user.setRealName(request.realName().trim());
        user.setNickname(request.nickname().trim());
        user.setGender(request.gender());
        user.setPhone(normalize(request.phone()));
        user.setEmail(request.email().trim());
        user.setBio(normalize(request.bio()));
        user.setUpdatedBy(loginUser.username());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse uploadAvatar(LoginUser loginUser, MultipartFile file) {
        SysUser user = requireActiveUser(loginUser.userId());
        AvatarStorageService.StoredAvatar storedAvatar = avatarStorageService.store(file);
        String previousAvatar = user.getAvatar();
        user.setAvatar(storedAvatar.url());
        user.setUpdatedBy(loginUser.username());
        user.setUpdatedAt(LocalDateTime.now());
        try {
            userMapper.updateById(user);
        } catch (RuntimeException exception) {
            avatarStorageService.deleteQuietly(storedAvatar.storageName());
            throw exception;
        }
        avatarStorageService.deleteByUrl(previousAvatar);
        return toResponse(user);
    }

    @Transactional
    public void changePassword(LoginUser loginUser, PasswordChangeRequest request) {
        SysUser user = requireActiveUser(loginUser.userId());
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("当前密码不正确");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与当前密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedBy(loginUser.username());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private UserProfileResponse toResponse(SysUser user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getNickname(),
                user.getGender(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getAddress(),
                user.getBio(),
                userMapper.selectRoleCodesByUserId(user.getId()),
                user.getCreatedAt()
        );
    }

    private SysUser requireActiveUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "账号不存在或已停用");
        }
        return user;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
