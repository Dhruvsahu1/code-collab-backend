package com.codesync.file.dto;

import java.time.LocalDateTime;

public class FileResponse {
    private Long fileId;
    private Long projectId;
    private String name;
    private String path;
    private String language;
    private Long size;
    private Long createdById;
    private Long lastEditedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private boolean isFolder;

    public FileResponse() {}

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public Long getLastEditedBy() { return lastEditedBy; }
    public void setLastEditedBy(Long lastEditedBy) { this.lastEditedBy = lastEditedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public boolean isFolder() { return isFolder; }
    public void setFolder(boolean folder) { isFolder = folder; }
}
