package com.example.managesystem.service.impl;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.dto.LoginRequest;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.security.CustomUserDetailsService;
import com.example.managesystem.security.JwtTokenProvider;
import com.example.managesystem.security.LoginAttemptGuard;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.security.AuthSessionService;
import com.example.managesystem.service.AuthService;
import com.example.managesystem.vo.LoginResponse;
import com.example.managesystem.vo.UserInfoResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final SysUserMapper userMapper;
    private final AuthSessionService sessionService;
    private final LoginAttemptGuard loginAttemptGuard;

    public LoginResponse login(LoginRequest request, String clientAddress) {
        loginAttemptGuard.checkAllowed(request.userName(), clientAddress);
        LoginUser loginUser;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.userName(), request.password())
            );
            loginUser = (LoginUser) authentication.getPrincipal();
        } catch (AuthenticationException exception) {
            loginAttemptGuard.recordFailure(request.userName(), clientAddress);
            throw new BusinessException(400, "用户名或密码错误");
        }
        loginAttemptGuard.recordSuccess(request.userName(), clientAddress);
        return sessionService.create(loginUser);
    }

    public LoginResponse refresh(String refreshToken) {
        try {
            if (!tokenProvider.isRefreshToken(refreshToken)) {
                throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "刷新令牌无效");
            }
            LoginUser loginUser = userDetailsService.loadUserById(tokenProvider.getUserId(refreshToken));
            if (!tokenProvider.isValid(refreshToken, loginUser)) {
                throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "刷新令牌已失效");
            }
            return sessionService.rotate(loginUser, refreshToken);
        } catch (JwtException | IllegalArgumentException | AuthenticationException exception) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "刷新令牌已失效");
        }
    }

    public UserInfoResponse currentUser(LoginUser loginUser) {
        var user = userMapper.selectById(loginUser.userId());
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "账号不存在或已停用");
        }
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        return new UserInfoResponse(
                permissions,
                roles,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar()
        );
    }

}
