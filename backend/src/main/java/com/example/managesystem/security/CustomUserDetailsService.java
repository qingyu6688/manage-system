package com.example.managesystem.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final PermissionPolicy permissionPolicy;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        return toLoginUser(user);
    }

    public LoginUser loadUserById(Long id) {
        SysUser user = id == null ? null : userMapper.selectById(id);
        if (user == null) { throw new UsernameNotFoundException("账号不存在或已注销"); }
        return toLoginUser(user);
    }

    private LoginUser toLoginUser(SysUser user) {
        List<String> authorityCodes = new ArrayList<>(userMapper.selectRoleCodesByUserId(user.getId()));
        authorityCodes.addAll(permissionPolicy.forUser(user.getId()));
        List<SimpleGrantedAuthority> authorities = authorityCodes.stream()
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                Integer.valueOf(1).equals(user.getStatus()), authorities,
                user.getTokenVersion() == null ? 0 : user.getTokenVersion());
    }
}
