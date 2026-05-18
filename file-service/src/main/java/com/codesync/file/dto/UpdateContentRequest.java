package com.codesync.file.dto;

public class UpdateContentRequest {
    private String content;
    private Long userId;

    public UpdateContentRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
