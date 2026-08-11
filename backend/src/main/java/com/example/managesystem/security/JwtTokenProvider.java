package com.example.managesystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Duration accessTokenDuration;
    private final Duration refreshTokenDuration;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-minutes}") long accessTokenMinutes,
            @Value("${security.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET 至少需要32字节");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenDuration = Duration.ofMinutes(accessTokenMinutes);
        this.refreshTokenDuration = Duration.ofDays(refreshTokenDays);
    }

    public String createAccessToken(LoginUser user) {
        return createToken(user.getUsername(), user.userId(), "access", accessTokenDuration);
    }

    public String createRefreshToken(LoginUser user) {
        return createToken(user.getUsername(), user.userId(), "refresh", refreshTokenDuration);
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseClaims(token).get("type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().after(new Date());
    }

    private String createToken(String username, Long userId, String type, Duration duration) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(duration)))
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
