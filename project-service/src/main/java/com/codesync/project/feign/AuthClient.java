package com.codesync.project.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${app.services.auth-service}")
public interface AuthClient {

    @GetMapping("/auth/profile/{userId}")
    UserResponse getUserProfile(@PathVariable("userId") Long userId);

    @GetMapping("/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);
}

class UserResponse {
    private Long userId;
    private String username;
    private String email;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}