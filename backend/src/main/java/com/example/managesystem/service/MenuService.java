package com.example.managesystem.service;

import com.example.managesystem.dto.MenuSaveRequest;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.vo.MenuRoute;

import java.util.List;

public interface MenuService {

    List<MenuRoute> currentUserMenus(LoginUser loginUser);

    List<MenuRoute> allMenus();

    Long create(MenuSaveRequest request);

    void update(Long id, MenuSaveRequest request);

    void delete(Long id);
}
