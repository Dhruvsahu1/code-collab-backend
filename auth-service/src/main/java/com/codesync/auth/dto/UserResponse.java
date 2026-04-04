package com.codesync.auth.dto;

import com.codesync.auth.enums.Role;

import java.time.LocalDateTime;

public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private String avatarUrl;
    private String provider;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String bio;

    public UserResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UserResponse response = new UserResponse();

        public Builder userId(Long userId) { response.userId = userId; return this; }
        public Builder username(String username) { response.username = username; return this; }
        public Builder email(String email) { response.email = email; return this; }
        public Builder fullName(String fullName) { response.fullName = fullName; return this; }
        public Builder role(Role role) { response.role = role; return this; }
        public Builder avatarUrl(String avatarUrl) { response.avatarUrl = avatarUrl; return this; }
        public Builder provider(String provider) { response.provider = provider; return this; }
        public Builder isActive(Boolean isActive) { response.isActive = isActive; return this; }
        public Builder createdAt(LocalDateTime createdAt) { response.createdAt = createdAt; return this; }
        public Builder bio(String bio) { response.bio = bio; return this; }
        public UserResponse build() { return response; }
    }
}