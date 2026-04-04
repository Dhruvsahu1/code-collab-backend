package com.codesync.auth.security;

import com.codesync.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {

    private Long userId;
    private String username;
    private String email;
    private String password;
    private String role;
    private boolean isActive;

    private UserDetailsImpl() {}

    public static UserDetailsImpl build(User user) {
        UserDetailsImpl impl = new UserDetailsImpl();
        impl.userId = user.getUserId();
        impl.username = user.getUsername();
        impl.email = user.getEmail();
        impl.password = user.getPasswordHash();
        impl.role = user.getRole().name();
        impl.isActive = user.getIsActive();
        return impl;
    }

    public Long getUserId() { return userId; }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return isActive; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}