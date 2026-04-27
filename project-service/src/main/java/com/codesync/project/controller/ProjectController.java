package com.codesync.project.controller;

import com.codesync.project.dto.CreateProjectRequest;
import com.codesync.project.dto.ProjectResponse;
import com.codesync.project.dto.UpdateProjectRequest;
import com.codesync.project.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Project service is running!");
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        logger.info("Creating project '{}' by user: {}", request.getName(), userId);
        return ResponseEntity.ok(projectService.createProject(request, userId));
    }

    @PostMapping("/fork/{projectId}")
    public ResponseEntity<ProjectResponse> forkProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        logger.info("Forking project {} by user: {}", projectId, userId);
        return ResponseEntity.ok(projectService.forkProject(projectId, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    @GetMapping("/owner")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByOwner(
            @RequestParam Long ownerId,
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

    @GetMapping("/member")
    public ResponseEntity<?> getProjectsByMember(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(projectService.getProjectsByMember(userId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardProjects(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(projectService.getDashboardProjects(userId));
    }

    @GetMapping("/language")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByLanguage(
            @RequestParam String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(projectService.getProjectsByLanguage(lang, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(projectService.updateProject(id, request, userId));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archiveProject(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        projectService.archiveProject(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/star")
    public ResponseEntity<Map<String, Boolean>> starProject(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        boolean isStarred = projectService.toggleStarProject(id, userId);
        return ResponseEntity.ok(Map.of("starred", isStarred));
    }

    @GetMapping("/{id}/star")
    public ResponseEntity<Map<String, Boolean>> isProjectStarred(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        boolean isStarred = projectService.isProjectStarred(id, userId);
        return ResponseEntity.ok(Map.of("starred", isStarred));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        projectService.deleteProject(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        try {
            return (Long) authentication.getPrincipal();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid authentication");
        }
    }
}
