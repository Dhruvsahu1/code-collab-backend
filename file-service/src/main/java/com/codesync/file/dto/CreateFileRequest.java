package com.codesync.file.dto;

public class CreateFileRequest {
    private Long projectId;
    private String name;
    private String path;
    private String language;
    private String content;
    private Long createdById;

    public CreateFileRequest() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
}
