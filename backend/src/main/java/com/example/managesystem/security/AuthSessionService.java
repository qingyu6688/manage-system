package com.example.managesystem.security;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.mapper.AuthSessionMapper;
import com.example.managesystem.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionService {
    private final AuthSessionMapper sessionMapper;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public LoginResponse create(LoginUser user) {
        String id = UUID.randomUUID().toString();
        LoginResponse tokens = tokens(user, id);
        sessionMapper.purgeExpired();
        LocalDateTime expiry = LocalDateTime.ofInstant(
                tokenProvider.getExpiration(tokens.refreshToken()), ZoneId.of("Asia/Shanghai"));
        sessionMapper.create(id, user.userId(), hash(tokens.refreshToken()), expiry);
        return tokens;
    }

    @Transactional
    public LoginResponse rotate(LoginUser user, String previousToken) {
        String id = tokenProvider.getSessionId(previousToken);
        LoginResponse tokens = tokens(user, id);
        if (sessionMapper.rotate(id, user.userId(), hash(previousToken), hash(tokens.refreshToken())) != 1) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED, "会话已失效，请重新登录");
        }
        return tokens;
    }

    public boolean isActive(String token, LoginUser user) {
        String id = tokenProvider.getSessionId(token);
        return id != null && sessionMapper.active(id, user.userId()) == 1;
    }

    public void revoke(String token, LoginUser user) {
        sessionMapper.revoke(tokenProvider.getSessionId(token), user.userId());
    }

    private LoginResponse tokens(LoginUser user, String id) {
        return new LoginResponse(tokenProvider.createAccessToken(user, id), tokenProvider.createRefreshToken(user, id));
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境不支持会话摘要算法", exception);
        }
    }
}
