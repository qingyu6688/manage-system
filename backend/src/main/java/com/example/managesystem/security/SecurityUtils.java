package com.example.managesystem.security;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(
                    ErrorCode.AUTHENTICATION_REQUIRED,
                    ErrorCode.AUTHENTICATION_REQUIRED.defaultMessage()
            );
        }
        return loginUser;
    }

    public static String currentUsername() {
        return currentUser().username();
    }
}
