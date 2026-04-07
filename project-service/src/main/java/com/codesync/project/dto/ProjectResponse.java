package com.codesync.project.dto;

import com.codesync.project.enums.Visibility;

import java.time.LocalDateTime;

public class ProjectResponse {
    private Long projectId;
    private Long ownerId;
    private String name;
    private String description;
    private String language;
    private Visibility visibility;
    private Long templateId;
    private boolean isArchived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int starCount;
    private int forkCount;

    public ProjectResponse() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getStarCount() { return starCount; }
    public void setStarCount(int starCount) { this.starCount = starCount; }

    public int getForkCount() { return forkCount; }
    public void setForkCount(int forkCount) { this.forkCount = forkCount; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private ProjectResponse response = new ProjectResponse();

        public Builder projectId(Long projectId) { response.projectId = projectId; return this; }
        public Builder ownerId(Long ownerId) { response.ownerId = ownerId; return this; }
        public Builder name(String name) { response.name = name; return this; }
        public Builder description(String description) { response.description = description; return this; }
        public Builder language(String language) { response.language = language; return this; }
        public Builder visibility(Visibility visibility) { response.visibility = visibility; return this; }
        public Builder templateId(Long templateId) { response.templateId = templateId; return this; }
        public Builder archived(boolean archived) { response.isArchived = archived; return this; }
        public Builder createdAt(LocalDateTime createdAt) { response.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { response.updatedAt = updatedAt; return this; }
        public Builder starCount(int starCount) { response.starCount = starCount; return this; }
        public Builder forkCount(int forkCount) { response.forkCount = forkCount; return this; }
        public ProjectResponse build() { return response; }
    }
}