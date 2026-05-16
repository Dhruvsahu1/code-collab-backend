package com.codesync.project.dto;

public class AddCollaboratorRequest {
    private Long userId;

    public AddCollaboratorRequest() {
    }

    public AddCollaboratorRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
