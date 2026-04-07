package com.codesync.file.dto;

public class CreateFolderRequest {
    private Long projectId;
    private String name;
    private String path;
    private Long createdById;

    public CreateFolderRequest() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
}
