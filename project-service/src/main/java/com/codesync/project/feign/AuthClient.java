package com.codesync.project.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.codesync.project.dto.UserResponse;

/**
 * Feign client for interacting with the Auth Service.
 */
@FeignClient(name = "auth-service", url = "${app.services.auth-service}")
public interface AuthClient {

    @GetMapping("/auth/profile/{userId}")
    UserResponse getUserProfile(@PathVariable("userId") Long userId);

    @GetMapping("/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);

    @GetMapping("/auth/search")
    Page<UserResponse> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String token);

    // Fallback class for when auth service is unavailable
    @Component
    class Fallback implements AuthClient {
        @Override
        public UserResponse getUserProfile(Long userId) {
            return null;
        }

        @Override
        public boolean validateToken(String token) {
            return false;
        }

        @Override
        public Page<UserResponse> searchUsers(String query, String q, int page, int size, String token) {
            return null;
        }
    }
}
