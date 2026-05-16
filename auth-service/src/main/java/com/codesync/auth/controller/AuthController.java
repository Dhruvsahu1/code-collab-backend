package com.codesync.auth.controller;

import com.codesync.auth.dto.*;
import com.codesync.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        logger.info("API HIT: POST /auth/register - username: {}", request.getUsername());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        logger.info("API HIT: POST /auth/login - usernameOrEmail: {}", request.getUsernameOrEmail());
        if (request.getUsernameOrEmail() == null || request.getUsernameOrEmail().isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("API HIT: POST /auth/refresh");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        logger.info("API HIT: GET /auth/profile");
        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> getProfileById(@PathVariable Long userId) {
        logger.info("API HIT: GET /auth/profile/{}", userId);
        return ResponseEntity.ok(authService.getProfileById(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        logger.info("API HIT: PUT /auth/profile");
        return ResponseEntity.ok(authService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        logger.info("API HIT: PUT /auth/password");
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String searchTerm = (q != null) ? q : query;
        logger.info("API HIT: GET /auth/search - query: {}", searchTerm);
        return ResponseEntity.ok(authService.searchUsers(searchTerm, page, size));
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<Void> deactivateAccount(Authentication authentication) {
        logger.info("API HIT: DELETE /auth/deactivate");
        authService.deactivateAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/users/all")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        logger.info("API HIT: GET /auth/users/all");
        return ResponseEntity.ok(authService.getAllUsers());
    }
}