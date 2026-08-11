package com.example.managesystem.service;

import com.example.managesystem.dto.LoginRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.vo.LoginResponse;
import com.example.managesystem.vo.UserInfoResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(String refreshToken);

    UserInfoResponse currentUser(LoginUser loginUser);
}
