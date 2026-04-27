package com.codesync.auth.service;

import com.codesync.auth.dto.*;
import com.codesync.auth.entity.User;
import com.codesync.auth.enums.AuthProvider;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse registerWithProvider(RegisterRequest request, AuthProvider provider);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserResponse getProfile(String username);

    UserResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    Page<UserResponse> searchUsers(String query, int page, int size);

    void deactivateAccount(String username);
    
    List<Map<String, Object>> getAllUsers();
}