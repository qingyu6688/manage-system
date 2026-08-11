package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.config.OperationLogInterceptor;
import com.example.managesystem.dto.LoginRequest;
import com.example.managesystem.dto.RefreshTokenRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.AuthService;
import com.example.managesystem.vo.LoginResponse;
import com.example.managesystem.vo.UserInfoResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpRequest
    ) {
        httpRequest.setAttribute(
                OperationLogInterceptor.LOGIN_USERNAME_ATTRIBUTE,
                loginRequest.userName()
        );
        return ApiResponse.success("登录成功", authService.login(loginRequest));
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request.refreshToken()));
    }

    @GetMapping("/user/info")
    public ApiResponse<UserInfoResponse> currentUser(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.success(authService.currentUser(loginUser));
    }
}
