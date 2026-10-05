package com.example.managesystem.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record LoginUser(
        Long userId,
        String username,
        String password,
        boolean enabled,
        Collection<? extends GrantedAuthority> authorities,
        long tokenVersion
) implements UserDetails {

    public LoginUser(Long userId, String username, String password, boolean enabled,
                     Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, password, enabled, authorities, 0);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
