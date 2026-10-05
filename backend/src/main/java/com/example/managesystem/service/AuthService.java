package com.example.managesystem.service;

import com.example.managesystem.dto.LoginRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.vo.LoginResponse;
import com.example.managesystem.vo.UserInfoResponse;

public interface AuthService {

    /**
     * @param clientAddress 请求来源地址，用于登录失败限流
     */
    LoginResponse login(LoginRequest request, String clientAddress);

    LoginResponse refresh(String refreshToken);

    UserInfoResponse currentUser(LoginUser loginUser);
}
