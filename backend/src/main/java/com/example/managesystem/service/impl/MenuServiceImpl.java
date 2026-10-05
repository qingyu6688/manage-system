package com.example.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.dto.MenuSaveRequest;
import com.example.managesystem.entity.SysMenu;
import com.example.managesystem.mapper.SysMenuMapper;
import com.example.managesystem.mapper.SysRoleMenuMapper;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.security.PermissionPolicy;
import com.example.managesystem.service.MenuService;
import com.example.managesystem.vo.MenuRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private static final String BUTTON_TYPE = "BUTTON";

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public List<MenuRoute> currentUserMenus(LoginUser loginUser) {
        // 超级管理员在接口层不受菜单授权限制，菜单也直接取全部启用节点；
        // 否则误取消 R_SUPER 的“角色管理”授权后，界面上将无法再改回来。
        if (PermissionPolicy.isSuper(loginUser)) {
            return buildTree(menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getEnabled, true)));
        }
        return buildTree(menuMapper.selectEnabledByUserId(loginUser.userId()));
    }

    public List<MenuRoute> allMenus() {
        return buildTree(menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId)));
    }

    @Transactional
    public Long create(MenuSaveRequest request) {
        validateRequest(null, request);
        SysMenu menu = new SysMenu();
        applyRequest(menu, request);
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Transactional
    public void update(Long id, MenuSaveRequest request) {
        SysMenu menu = requireMenu(id);
        validateRequest(id, request);
        if (Objects.equals(id, defaultParentId(request.parentId()))) {
            throw new BusinessException("菜单不能设置自己为上级节点");
        }
        ensureParentNotDescendant(id, defaultParentId(request.parentId()));
        // 后端接口权限串由路由名称拼成（如 User:add），改名会让已授权角色在接口层全部失去权限。
        if (!BUTTON_TYPE.equals(menu.getMenuType()) && !BUTTON_TYPE.equals(request.menuType())
                && menu.getRouteName() != null && !menu.getRouteName().equals(trimToNull(request.routeName()))) {
            throw new BusinessException("路由名称创建后不能修改，如需调整请新建菜单");
        }
        if (!BUTTON_TYPE.equals(menu.getMenuType()) && BUTTON_TYPE.equalsIgnoreCase(request.menuType())) {
            Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getParentId, id));
            if (childCount > 0) {
                throw new BusinessException("存在子节点的菜单不能转换为按钮");
            }
        }
        applyRequest(menu, request);
        menu.setUpdatedAt(LocalDateTime.now());
        menuMapper.updateById(menu);
    }

    @Transactional
    public void delete(Long id) {
        requireMenu(id);
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("请先删除该节点下的子菜单或按钮");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<com.example.managesystem.entity.SysRoleMenu>()
                .eq(com.example.managesystem.entity.SysRoleMenu::getMenuId, id));
        menuMapper.deleteById(id);
    }

    private List<MenuRoute> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> childrenByParent = new HashMap<>();
        menus.stream()
                .sorted(Comparator.comparing(SysMenu::getSort).thenComparing(SysMenu::getId))
                .forEach(menu -> childrenByParent
                        .computeIfAbsent(defaultParentId(menu.getParentId()), ignored -> new ArrayList<>())
                        .add(menu));
        return buildRoutes(0L, childrenByParent);
    }

    private List<MenuRoute> buildRoutes(Long parentId, Map<Long, List<SysMenu>> childrenByParent) {
        List<SysMenu> children = childrenByParent.getOrDefault(parentId, List.of());
        List<MenuRoute> routes = new ArrayList<>();
        for (SysMenu menu : children) {
            if (BUTTON_TYPE.equals(menu.getMenuType())) {
                continue;
            }
            List<MenuRoute.AuthItem> authList = childrenByParent.getOrDefault(menu.getId(), List.of()).stream()
                    .filter(child -> BUTTON_TYPE.equals(child.getMenuType()))
                    .map(child -> new MenuRoute.AuthItem(
                            child.getId(),
                            child.getTitle(),
                            child.getPermission(),
                            child.getEnabled(),
                            child.getSort()
                    ))
                    .toList();
            List<MenuRoute> routeChildren = buildRoutes(menu.getId(), childrenByParent);
            MenuRoute.MenuMeta meta = new MenuRoute.MenuMeta(
                    menu.getTitle(),
                    menu.getIcon(),
                    menu.getHidden(),
                    menu.getKeepAlive(),
                    menu.getHiddenTab(),
                    menu.getExternalLink(),
                    menu.getIframe(),
                    menu.getBadge(),
                    menu.getTextBadge(),
                    menu.getFixedTab(),
                    menu.getActivePath(),
                    menu.getFullPage(),
                    menu.getEnabled(),
                    menu.getSort(),
                    authList
            );
            routes.add(new MenuRoute(
                    menu.getId(),
                    menu.getParentId(),
                    menu.getPath(),
                    menu.getRouteName(),
                    menu.getComponent(),
                    meta,
                    routeChildren
            ));
        }
        return routes;
    }

    private void validateRequest(Long id, MenuSaveRequest request) {
        Long parentId = defaultParentId(request.parentId());
        SysMenu parent = parentId > 0 ? requireMenu(parentId) : null;
        if (parent != null && BUTTON_TYPE.equals(parent.getMenuType())) {
            throw new BusinessException("按钮节点不能作为上级节点");
        }
        if (BUTTON_TYPE.equals(request.menuType())) {
            // 按钮权限串依赖上级菜单的路由名称，挂在根节点下不会产生任何有效权限。
            if (parent == null) {
                throw new BusinessException("按钮必须挂在某个菜单下");
            }
            String permission = trimToNull(request.permission());
            if (permission == null) {
                throw new BusinessException("按钮权限标识不能为空");
            }
            long duplicated = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getParentId, parentId)
                    .eq(SysMenu::getMenuType, BUTTON_TYPE)
                    .eq(SysMenu::getPermission, permission)
                    .ne(id != null, SysMenu::getId, id));
            if (duplicated > 0) {
                throw new BusinessException("同一菜单下的按钮权限标识不能重复");
            }
        } else {
            String routeName = trimToNull(request.routeName());
            if (routeName == null || trimToNull(request.path()) == null) {
                throw new BusinessException("菜单的路由名称和路由地址不能为空");
            }
            long duplicated = menuMapper.countReservedName(routeName, id);
            if (duplicated > 0) {
                throw new BusinessException("路由名称已使用，包含已删除菜单，请使用其他名称");
            }
        }
    }

    private void applyRequest(SysMenu menu, MenuSaveRequest request) {
        boolean button = BUTTON_TYPE.equals(request.menuType());
        menu.setParentId(defaultParentId(request.parentId()));
        menu.setMenuType(request.menuType());
        menu.setTitle(request.title().trim());
        // 按钮只保留权限标识，菜单只保留路由信息，另一类字段一律写 NULL，避免空串撞唯一索引。
        menu.setRouteName(button ? null : trimToNull(request.routeName()));
        menu.setPath(button ? null : trimToNull(request.path()));
        menu.setComponent(button ? null : trimToNull(request.component()));
        menu.setPermission(button ? trimToNull(request.permission()) : null);
        menu.setIcon(button ? null : trimToNull(request.icon()));
        menu.setSort(request.sort() == null ? 0 : request.sort());
        menu.setEnabled(request.enabled());
        menu.setHidden(Boolean.TRUE.equals(request.hidden()));
        menu.setKeepAlive(Boolean.TRUE.equals(request.keepAlive()));
        menu.setHiddenTab(Boolean.TRUE.equals(request.hiddenTab()));
        menu.setIframe(Boolean.TRUE.equals(request.iframe()));
        menu.setExternalLink(button ? null : trimToNull(request.externalLink()));
        menu.setBadge(Boolean.TRUE.equals(request.badge()));
        menu.setTextBadge(button ? null : trimToNull(request.textBadge()));
        menu.setFixedTab(Boolean.TRUE.equals(request.fixedTab()));
        menu.setActivePath(button ? null : trimToNull(request.activePath()));
        menu.setFullPage(Boolean.TRUE.equals(request.fullPage()));
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private SysMenu requireMenu(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "菜单节点不存在");
        }
        return menu;
    }

    private void ensureParentNotDescendant(Long menuId, Long parentId) {
        Set<Long> visited = new HashSet<>();
        Long currentId = parentId;
        while (currentId != null && currentId > 0) {
            if (Objects.equals(currentId, menuId)) {
                throw new BusinessException("不能把菜单移动到自己的子节点下");
            }
            if (!visited.add(currentId)) {
                throw new BusinessException("检测到异常的菜单层级循环");
            }
            currentId = requireMenu(currentId).getParentId();
        }
    }

    private Long defaultParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
