package com.codesync.version.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SnapshotResponse {
    
    private Long snapshotId;
    private Long projectId;
    private Long fileId;
    private Long authorId;
    private String message;
    private String content;
    private String hash;
    private Long parentSnapshotId;
    private String branch;
    private String tag;
    private LocalDateTime createdAt;
    private String authorName;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public Long getParentSnapshotId() { return parentSnapshotId; }
    public void setParentSnapshotId(Long parentSnapshotId) { this.parentSnapshotId = parentSnapshotId; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public static SnapshotResponse fromEntity(com.codesync.version.entity.Snapshot snapshot) {
        SnapshotResponse response = new SnapshotResponse();
        response.setSnapshotId(snapshot.getSnapshotId());
        response.setProjectId(snapshot.getProjectId());
        response.setFileId(snapshot.getFileId());
        response.setAuthorId(snapshot.getAuthorId());
        response.setMessage(snapshot.getMessage());
        response.setContent(snapshot.getContent());
        response.setHash(snapshot.getHash());
        response.setParentSnapshotId(snapshot.getParentSnapshotId());
        response.setBranch(snapshot.getBranch());
        response.setTag(snapshot.getTag());
        response.setCreatedAt(snapshot.getCreatedAt());
        return response;
    }
}