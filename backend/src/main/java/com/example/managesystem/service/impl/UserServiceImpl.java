package com.example.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.dto.UserSaveRequest;
import com.example.managesystem.entity.SysRole;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.entity.SysUserRole;
import com.example.managesystem.mapper.SysRoleMapper;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.mapper.SysUserRoleMapper;
import com.example.managesystem.security.SecurityUtils;
import com.example.managesystem.security.PermissionPolicy;
import com.example.managesystem.service.UserService;
import com.example.managesystem.vo.PageResponse;
import com.example.managesystem.vo.UserListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Long BUILT_IN_ADMIN_ID = 1L;

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionPolicy permissionPolicy;

    public PageResponse<UserListItem> page(
            long current,
            long size,
            String userName,
            String userGender,
            String userPhone,
            String userEmail,
            String status
    ) {
        long safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(userName), SysUser::getUsername, userName)
                .eq(StringUtils.hasText(userGender), SysUser::getGender, userGender)
                .like(StringUtils.hasText(userPhone), SysUser::getPhone, userPhone)
                .like(StringUtils.hasText(userEmail), SysUser::getEmail, userEmail)
                .eq(StringUtils.hasText(status), SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreatedAt);
        IPage<SysUser> page = userMapper.selectPage(new Page<>(Math.max(current, 1), safeSize), wrapper);
        // 一页最多100个用户，角色关系和角色信息各查一次，避免逐行查询。
        Map<Long, List<Long>> roleIdsByUser = loadRoleIds(page.getRecords());
        Map<Long, SysRole> roles = loadRoles(roleIdsByUser);
        IPage<UserListItem> converted = page.convert(user -> toListItem(
                user, roleIdsByUser.getOrDefault(user.getId(), List.of()), roles));
        return PageResponse.from(converted);
    }

    @Transactional
    public Long create(UserSaveRequest request) {
        validateUsernameUnique(null, request.userName());
        validateRoles(request.roleIds());
        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException("新增用户时必须设置初始密码");
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        applyRequest(user, request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedBy(SecurityUtils.currentUsername());
        user.setUpdatedBy(SecurityUtils.currentUsername());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        replaceRoles(user.getId(), request.roleIds());
        return user.getId();
    }

    @Transactional
    public void update(Long id, UserSaveRequest request) {
        SysUser user = requireUser(id);
        List<Long> currentRoleIds = userRoleMapper.selectRoleIdsByUserId(id);
        permissionPolicy.requireAssignable(currentRoleIds);
        String username = request.userName().trim();
        int status = request.status() == null ? 1 : request.status();
        boolean roleChanged = !Set.copyOf(currentRoleIds).equals(new HashSet<>(request.roleIds()));
        if (BUILT_IN_ADMIN_ID.equals(id) && (status != 1 || !user.getUsername().equals(username) || roleChanged)) {
            throw new BusinessException("内置管理员的账号、启用状态和角色不能修改");
        }
        if (id.equals(SecurityUtils.currentUser().userId()) && status != 1) {
            throw new BusinessException("不能停用当前登录账号");
        }
        validateUsernameUnique(id, username);
        validateRoles(request.roleIds());
        // 只有影响登录身份或权限的字段变化才让旧令牌失效，改昵称、手机号等不应把对方踢下线。
        boolean revokeSessions = roleChanged
                || !user.getUsername().equals(username)
                || !Objects.equals(user.getStatus(), status)
                || StringUtils.hasText(request.password());
        applyRequest(user, request);
        if (StringUtils.hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setUpdatedBy(SecurityUtils.currentUsername());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        if (roleChanged) {
            replaceRoles(id, request.roleIds());
        }
        if (revokeSessions) {
            userMapper.revokeTokens(id);
        }
    }

    @Transactional
    public void delete(Long id) {
        if (BUILT_IN_ADMIN_ID.equals(id)) {
            throw new BusinessException("内置管理员不能注销");
        }
        requireUser(id);
        if (id.equals(SecurityUtils.currentUser().userId())) {
            throw new BusinessException("不能注销当前登录账号");
        }
        permissionPolicy.requireAssignable(userRoleMapper.selectRoleIdsByUserId(id));
        userRoleMapper.deleteByUserId(id);
        userMapper.deleteById(id);
    }

    private Map<Long, List<Long>> loadRoleIds(List<SysUser> users) {
        if (users.isEmpty()) {
            return Map.of();
        }
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getUserId, users.stream().map(SysUser::getId).toList())
                        .orderByAsc(SysUserRole::getRoleId))
                .stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
    }

    private Map<Long, SysRole> loadRoles(Map<Long, List<Long>> roleIdsByUser) {
        List<Long> roleIds = roleIdsByUser.values().stream().flatMap(List::stream).distinct().toList();
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return roleMapper.selectByIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));
    }

    private UserListItem toListItem(SysUser user, List<Long> roleIds, Map<Long, SysRole> roles) {
        List<String> roleCodes = roleIds.stream()
                .map(roles::get)
                .filter(Objects::nonNull)
                .map(SysRole::getCode)
                .toList();
        return new UserListItem(
                user.getId(),
                user.getAvatar(),
                String.valueOf(user.getStatus()),
                user.getUsername(),
                Integer.valueOf(2).equals(user.getGender()) ? "女" : "男",
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                roleIds,
                roleCodes,
                user.getCreatedBy(),
                user.getCreatedAt(),
                user.getUpdatedBy(),
                user.getUpdatedAt()
        );
    }

    /**
     * 头像只能由本人在个人中心上传。管理员表单里的 avatar 字段为兼容旧前端保留但不再写入，
     * 否则管理员用打开弹窗时的旧地址保存，会覆盖用户刚上传的新头像，旧文件此时已被清理。
     */
    private void applyRequest(SysUser user, UserSaveRequest request) {
        user.setUsername(request.userName().trim());
        user.setNickname(request.nickName().trim());
        user.setGender(request.gender() == null ? 1 : request.gender());
        user.setPhone(trimToNull(request.userPhone()));
        user.setEmail(trimToNull(request.userEmail()));
        user.setStatus(request.status() == null ? 1 : request.status());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void validateUsernameUnique(Long id, String username) {
        long count = userMapper.countReservedName(username.trim(), id);
        if (count > 0) {
            throw new BusinessException("用户名已使用，包含已注销账号，请使用其他用户名");
        }
    }

    private void validateRoles(List<Long> roleIds) {
        permissionPolicy.requireAssignable(roleIds);
        long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getEnabled, true));
        if (count != roleIds.stream().distinct().count()) {
            throw new BusinessException("所选角色不存在或已停用");
        }
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        roleIds.stream().distinct().forEach(roleId -> userRoleMapper.insert(new SysUserRole(userId, roleId)));
    }
}
