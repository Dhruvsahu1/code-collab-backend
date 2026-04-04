package com.codesync.auth.service;

import com.codesync.auth.dto.*;
import com.codesync.auth.entity.User;
import org.springframework.data.domain.Page;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserResponse getProfile(String username);

    UserResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    Page<UserResponse> searchUsers(String query, int page, int size);

    void deactivateAccount(String username);
}