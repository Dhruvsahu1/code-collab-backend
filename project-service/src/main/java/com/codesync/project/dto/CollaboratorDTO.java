package com.codesync.project.dto;

import java.time.LocalDateTime;

public class CollaboratorDTO {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private LocalDateTime joinedAt;

    public CollaboratorDTO() {
    }

    public CollaboratorDTO(Long userId, String username, String email, String role, LocalDateTime joinedAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public static class Builder {
        private final CollaboratorDTO collaborator = new CollaboratorDTO();

        public Builder userId(Long userId) {
            collaborator.userId = userId;
            return this;
        }

        public Builder username(String username) {
            collaborator.username = username;
            return this;
        }

        public Builder email(String email) {
            collaborator.email = email;
            return this;
        }

        public Builder role(String role) {
            collaborator.role = role;
            return this;
        }

        public Builder joinedAt(LocalDateTime joinedAt) {
            collaborator.joinedAt = joinedAt;
            return this;
        }

        public CollaboratorDTO build() {
            return collaborator;
        }
    }
}
