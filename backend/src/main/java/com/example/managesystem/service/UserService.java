package com.example.managesystem.service;

import com.example.managesystem.dto.UserSaveRequest;
import com.example.managesystem.vo.PageResponse;
import com.example.managesystem.vo.UserListItem;

public interface UserService {

    PageResponse<UserListItem> page(
            long current,
            long size,
            String userName,
            String userGender,
            String userPhone,
            String userEmail,
            String status
    );

    Long create(UserSaveRequest request);

    void update(Long id, UserSaveRequest request);

    void delete(Long id);
}
