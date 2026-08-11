package com.example.managesystem.service;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.dto.PasswordChangeRequest;
import com.example.managesystem.dto.ProfileUpdateRequest;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.impl.ProfileServiceImpl;
import com.example.managesystem.vo.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AvatarStorageService avatarStorageService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private LoginUser loginUser;
    private SysUser user;

    @BeforeEach
    void setUp() {
        loginUser = new LoginUser(1L, "admin", "encoded-password", true, List.of());
        user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setNickname("管理员");
        user.setRealName("系统管理员");
        user.setGender(1);
        user.setEmail("admin@example.com");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.of(2026, 8, 11, 12, 0));
    }

    @Test
    void shouldReturnAndUpdateCurrentProfile() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("R_SUPER"));
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "新姓名",
                "新昵称",
                2,
                "13800000000",
                "new@example.com",
                "个人简介"
        );

        UserProfileResponse response = profileService.updateProfile(loginUser, request);

        assertEquals("新姓名", response.realName());
        assertEquals("新昵称", response.nickname());
        assertEquals("new@example.com", response.email());
        assertEquals(List.of("R_SUPER"), response.roles());
        verify(userMapper).updateById(user);
    }

    @Test
    void shouldUploadAvatarAndRemovePreviousManagedFile() {
        String previousAvatar = "/api/profile/avatar/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.png";
        user.setAvatar(previousAvatar);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );
        AvatarStorageService.StoredAvatar storedAvatar = new AvatarStorageService.StoredAvatar(
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.png",
                "/api/profile/avatar/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.png"
        );
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("R_SUPER"));
        when(avatarStorageService.store(file)).thenReturn(storedAvatar);

        UserProfileResponse response = profileService.uploadAvatar(loginUser, file);

        assertEquals(storedAvatar.url(), response.avatar());
        verify(userMapper).updateById(user);
        verify(avatarStorageService).deleteByUrl(previousAvatar);
    }

    @Test
    void shouldRejectWrongCurrentPassword() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> profileService.changePassword(
                        loginUser,
                        new PasswordChangeRequest("wrong-password", "NewPass123", "NewPass123")
                )
        );

        assertEquals("当前密码不正确", exception.getMessage());
    }

    @Test
    void shouldEncodeNewPassword() {
        when(userMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("Admin@123", "encoded-password")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123", "encoded-password")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("new-encoded-password");

        profileService.changePassword(
                loginUser,
                new PasswordChangeRequest("Admin@123", "NewPass123", "NewPass123")
        );

        assertEquals("new-encoded-password", user.getPassword());
        verify(userMapper).updateById(user);
    }
}
