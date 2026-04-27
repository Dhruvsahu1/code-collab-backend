package com.codesync.project.dto;

import com.codesync.project.enums.Visibility;

public class CreateProjectRequest {
    private Long ownerId;
    private String name;
    private String description;
    private String language;
    private String visibility; // Accept string from frontend
    private Long templateId;

    public CreateProjectRequest() {}

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    // Accept string visibility from frontend
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    
    // Return as enum for internal use
    public Visibility getVisibilityEnum() {
        if (visibility == null) return Visibility.PRIVATE;
        try {
            return Visibility.valueOf(visibility.toUpperCase());
        } catch (Exception e) {
            return Visibility.PRIVATE;
        }
    }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
}