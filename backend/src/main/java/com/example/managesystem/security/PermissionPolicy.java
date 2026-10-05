package com.example.managesystem.security;

import com.example.managesystem.entity.SysMenu;
import com.example.managesystem.entity.SysRole;
import com.example.managesystem.mapper.SysMenuMapper;
import com.example.managesystem.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionPolicy {
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;

    public static boolean isSuper(LoginUser user) {
        return user.getAuthorities().stream().anyMatch(authority -> "R_SUPER".equals(authority.getAuthority()));
    }

    public Set<String> forUser(Long userId) {
        return scopes(menuMapper.selectEnabledByUserId(userId));
    }

    public void requireAssignable(List<Long> roleIds) {
        LoginUser actor = SecurityUtils.currentUser();
        if (roleIds.isEmpty()) { return; }
        if (isSuper(actor)) { return; }
        Set<String> owned = new HashSet<>();
        actor.getAuthorities().forEach(authority -> owned.add(authority.getAuthority()));
        List<SysRole> roles = roleMapper.selectByIds(roleIds);
        if (roles.stream().anyMatch(role -> "R_SUPER".equals(role.getCode()))
                || !owned.containsAll(scopes(menuMapper.selectEnabledByRoleIds(roleIds)))) {
            throw new AccessDeniedException("不能分配或操作超出自身权限范围的角色");
        }
    }

    /** 只有上级菜单同时授权且启用，子菜单与按钮才生效。 */
    public static Set<String> scopes(List<SysMenu> menus) {
        Map<Long, SysMenu> byId = new HashMap<>();
        menus.forEach(menu -> byId.put(menu.getId(), menu));
        Set<String> result = new HashSet<>();
        for (SysMenu menu : menus) {
            if (!reachable(menu, byId)) { continue; }
            if ("MENU".equals(menu.getMenuType()) && menu.getRouteName() != null) {
                result.add(menu.getRouteName() + ":read");
            } else if ("BUTTON".equals(menu.getMenuType()) && menu.getPermission() != null) {
                SysMenu parent = byId.get(menu.getParentId());
                if (parent != null && parent.getRouteName() != null) {
                    result.add(parent.getRouteName() + ":" + menu.getPermission());
                }
            }
        }
        return result;
    }

    private static boolean reachable(SysMenu menu, Map<Long, SysMenu> byId) {
        Set<Long> visited = new HashSet<>();
        SysMenu node = menu;
        while (node != null) {
            if (!Boolean.TRUE.equals(node.getEnabled()) || !visited.add(node.getId())) { return false; }
            if (node.getParentId() == null || node.getParentId() == 0) { return true; }
            node = byId.get(node.getParentId());
        }
        return false;
    }
}
