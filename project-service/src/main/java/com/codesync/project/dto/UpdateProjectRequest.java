package com.codesync.project.dto;

import com.codesync.project.enums.Visibility;

public class UpdateProjectRequest {
    private String name;
    private String description;
    private String language;
    private Visibility visibility;

    public UpdateProjectRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
}