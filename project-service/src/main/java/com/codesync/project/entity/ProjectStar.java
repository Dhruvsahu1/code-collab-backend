package com.codesync.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_stars", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"projectId", "userId"}, name = "uk_project_star")
})
public class ProjectStar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime starredAt;

    public ProjectStar() {}

    public ProjectStar(Long projectId, Long userId) {
        this.projectId = projectId;
        this.userId = userId;
        this.starredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getStarredAt() { return starredAt; }
    public void setStarredAt(LocalDateTime starredAt) { this.starredAt = starredAt; }
}
