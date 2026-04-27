package com.codesync.version.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateSnapshotRequest {
    
    @NotNull
    private Long projectId;
    
    @NotNull
    private Long fileId;
    
    @NotNull
    private Long authorId;
    
    private String message;
    
    private String content;
    
    private String branch = "main";
    
    private Long parentSnapshotId;

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
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public Long getParentSnapshotId() { return parentSnapshotId; }
    public void setParentSnapshotId(Long parentSnapshotId) { this.parentSnapshotId = parentSnapshotId; }
}