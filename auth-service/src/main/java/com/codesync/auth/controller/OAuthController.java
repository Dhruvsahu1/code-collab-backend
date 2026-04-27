package com.codesync.auth.controller;

import com.codesync.auth.dto.AuthResponse;
import com.codesync.auth.dto.RegisterRequest;
import com.codesync.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthController {

    private final AuthService authService;

    public OAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login/google")
    public ResponseEntity<Map<String, String>> googleLogin() {
        Map<String, String> response = new HashMap<>();
        response.put("authorizationUri", "https://accounts.google.com/o/oauth2/v2/auth?" +
            "client_id={google-client-id}&" +
            "redirect_uri={baseUrl}/auth/oauth/callback/google&" +
            "response_type=code&" +
            "scope=openid%20email%20profile&" +
            "state=google");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login/github")
    public ResponseEntity<Map<String, String>> githubLogin() {
        Map<String, String> response = new HashMap<>();
        response.put("authorizationUri", "https://github.com/login/oauth/authorize?" +
            "client_id={github-client-id}&" +
            "redirect_uri={baseUrl}/auth/oauth/callback/github&" +
            "scope=read:user%20user:email&" +
            "state=github");
        return ResponseEntity.ok(response);
    }
}