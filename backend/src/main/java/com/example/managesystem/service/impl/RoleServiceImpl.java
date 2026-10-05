package com.example.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.dto.RoleSaveRequest;
import com.example.managesystem.entity.SysMenu;
import com.example.managesystem.entity.SysRole;
import com.example.managesystem.entity.SysRoleMenu;
import com.example.managesystem.entity.SysUserRole;
import com.example.managesystem.mapper.SysMenuMapper;
import com.example.managesystem.mapper.SysRoleMapper;
import com.example.managesystem.mapper.SysRoleMenuMapper;
import com.example.managesystem.mapper.SysUserRoleMapper;
import com.example.managesystem.service.RoleService;
import com.example.managesystem.security.PermissionPolicy;
import com.example.managesystem.vo.PageResponse;
import com.example.managesystem.vo.RoleListItem;
import com.example.managesystem.vo.RoleOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final String SUPER_ROLE_CODE = "R_SUPER";

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionPolicy permissionPolicy;

    public PageResponse<RoleListItem> page(
            long current,
            long size,
            String roleName,
            String roleCode,
            String description,
            Boolean enabled,
            LocalDate startTime,
            LocalDate endTime
    ) {
        long safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(roleName), SysRole::getName, roleName)
                .like(StringUtils.hasText(roleCode), SysRole::getCode, roleCode)
                .like(StringUtils.hasText(description), SysRole::getDescription, description)
                .eq(enabled != null, SysRole::getEnabled, enabled)
                .ge(startTime != null, SysRole::getCreatedAt, startTime == null ? null : startTime.atStartOfDay())
                .le(endTime != null, SysRole::getCreatedAt, endTime == null ? null : endTime.atTime(LocalTime.MAX))
                .orderByAsc(SysRole::getId);
        IPage<SysRole> page = roleMapper.selectPage(new Page<>(Math.max(current, 1), safeSize), wrapper);
        return PageResponse.from(page.convert(this::toListItem));
    }

    public List<RoleOption> options() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getEnabled, true)
                        .orderByAsc(SysRole::getId))
                .stream()
                .filter(this::canAssign)
                .map(role -> new RoleOption(role.getId(), role.getName(), role.getCode()))
                .toList();
    }

    public List<Long> getMenuIds(Long roleId) {
        requireRole(roleId);
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Transactional
    public Long create(RoleSaveRequest request) {
        validateCodeUnique(null, request.roleCode());
        SysRole role = new SysRole();
        applyRequest(role, request);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return role.getId();
    }

    @Transactional
    public void update(Long id, RoleSaveRequest request) {
        SysRole role = requireRole(id);
        if (SUPER_ROLE_CODE.equals(role.getCode()) && Boolean.FALSE.equals(request.enabled())) {
            throw new BusinessException("不能停用内置超级管理员角色");
        }
        if (SUPER_ROLE_CODE.equals(role.getCode()) && !SUPER_ROLE_CODE.equals(request.roleCode())) {
            throw new BusinessException("内置超级管理员角色编码不能修改");
        }
        validateCodeUnique(id, request.roleCode());
        applyRequest(role, request);
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = requireRole(id);
        if (SUPER_ROLE_CODE.equals(role.getCode())) {
            throw new BusinessException("内置超级管理员角色不能删除");
        }
        Long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException("该角色仍被用户使用，不能删除");
        }
        roleMenuMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);
    }

    @Transactional
    public void updateMenus(Long roleId, List<Long> menuIds) {
        SysRole role = requireRole(roleId);
        List<Long> distinctIds = menuIds.stream().distinct().toList();
        if (!distinctIds.isEmpty()) {
            long menuCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, distinctIds));
            if (menuCount != distinctIds.size()) {
                throw new BusinessException("菜单权限数据包含不存在的节点");
            }
        }
        roleMenuMapper.deleteByRoleId(roleId);
        distinctIds.forEach(menuId -> roleMenuMapper.insert(new SysRoleMenu(role.getId(), menuId)));
    }

    private RoleListItem toListItem(SysRole role) {
        return new RoleListItem(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                role.getEnabled(),
                role.getCreatedAt()
        );
    }

    private SysRole requireRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private void validateCodeUnique(Long id, String code) {
        long count = roleMapper.countReservedCode(code, id);
        if (count > 0) {
            throw new BusinessException("角色编码已使用，包含已删除角色，请使用其他编码");
        }
    }

    private void applyRequest(SysRole role, RoleSaveRequest request) {
        role.setName(request.roleName());
        role.setCode(request.roleCode());
        role.setDescription(request.description());
        role.setEnabled(request.enabled() == null || request.enabled());
    }

    private boolean canAssign(SysRole role) {
        try {
            permissionPolicy.requireAssignable(List.of(role.getId()));
            return true;
        } catch (org.springframework.security.access.AccessDeniedException exception) {
            return false;
        }
    }
}
