package com.codesync.project.controller;

import com.codesync.project.dto.CreateProjectRequest;
import com.codesync.project.dto.ProjectResponse;
import com.codesync.project.dto.UpdateProjectRequest;
import com.codesync.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PostMapping("/fork/{projectId}")
    public ResponseEntity<ProjectResponse> forkProject(
            @PathVariable Long projectId,
            @RequestParam Long newOwnerId) {
        return ResponseEntity.ok(projectService.forkProject(projectId, newOwnerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(ownerId, page, size));
    }

    @GetMapping("/public")
    public ResponseEntity<Page<ProjectResponse>> getPublicProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(projectService.getPublicProjects(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(projectService.searchProjects(keyword, page, size));
    }

    @GetMapping("/member/{userId}")
    public ResponseEntity<?> getProjectsByMember(@PathVariable Long userId) {
        return ResponseEntity.ok(projectService.getProjectsByMember(userId));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByLanguage(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(projectService.getProjectsByLanguage(language, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PutMapping("/archive/{id}")
    public ResponseEntity<Void> archiveProject(@PathVariable Long id) {
        projectService.archiveProject(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/star/{id}")
    public ResponseEntity<Void> starProject(@PathVariable Long id) {
        projectService.starProject(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}