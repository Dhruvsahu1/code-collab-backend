package com.codesync.auth.controller;

import com.codesync.auth.dto.AuthResponse;
import com.codesync.auth.dto.LoginRequest;
import com.codesync.auth.dto.RegisterRequest;
import com.codesync.auth.enums.AuthProvider;
import com.codesync.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthCallbackController {

    private final AuthService authService;

    public OAuthCallbackController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/callback/google")
    public ResponseEntity<AuthResponse> handleGoogleCallback(@RequestBody OAuthCallbackRequest request) {
        return handleOAuthCallback(request.getEmail(), request.getName(), request.getGoogleId(), AuthProvider.GOOGLE);
    }

    @PostMapping("/callback/github")
    public ResponseEntity<AuthResponse> handleGithubCallback(@RequestBody OAuthCallbackRequest request) {
        return handleOAuthCallback(request.getEmail(), request.getName(), request.getGithubId(), AuthProvider.GITHUB);
    }

    private ResponseEntity<AuthResponse> handleOAuthCallback(String email, String name, String providerId, AuthProvider provider) {
        try {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsernameOrEmail(email);
            loginRequest.setPassword(provider.name() + ":" + providerId);
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(email);
            registerRequest.setUsername(extractUsernameFromEmail(email));
            registerRequest.setPassword(provider.name() + ":" + providerId);
            registerRequest.setFullName(name);
            AuthResponse response = authService.registerWithProvider(registerRequest, provider);
            return ResponseEntity.ok(response);
        }
    }

    private String extractUsernameFromEmail(String email) {
        return email != null ? email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "_") : "user_" + System.currentTimeMillis();
    }

    public static class OAuthCallbackRequest {
        private String email;
        private String name;
        private String googleId;
        private String githubId;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGoogleId() { return googleId; }
        public void setGoogleId(String googleId) { this.googleId = googleId; }
        public String getGithubId() { return githubId; }
        public void setGithubId(String githubId) { this.githubId = githubId; }
    }
}