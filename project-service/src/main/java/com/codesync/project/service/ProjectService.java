package com.codesync.project.service;

import com.codesync.project.dto.CreateProjectRequest;
import com.codesync.project.dto.ProjectResponse;
import com.codesync.project.dto.UpdateProjectRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Project management.
 */
public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request, Long userId);

    ProjectResponse getProjectById(Long projectId, Long userId);

    List<ProjectResponse> getProjectsByOwner(Long ownerId);

    Page<ProjectResponse> getProjectsByOwner(Long ownerId, int page, int size);

    List<ProjectResponse> getPublicProjects();

    Page<ProjectResponse> getPublicProjects(int page, int size);

    Page<ProjectResponse> searchProjects(String keyword, int page, int size);

    List<ProjectResponse> getProjectsByMember(Long userId);

    List<ProjectResponse> getProjectsByLanguage(String language);

    Page<ProjectResponse> getProjectsByLanguage(String language, int page, int size);

    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long userId);

    void archiveProject(Long projectId, Long userId);

    void deleteProject(Long projectId, Long userId);

    ProjectResponse forkProject(Long projectId, Long userId);

    boolean toggleStarProject(Long projectId, Long userId);

    boolean isProjectStarred(Long projectId, Long userId);

    List<ProjectResponse> getDashboardProjects(Long userId);

    // Collaborator management
    void addCollaborator(Long projectId, Long userIdToAdd, Long currentUserId);

    void removeCollaborator(Long projectId, Long userIdToRemove, Long currentUserId);

    // Get collaborators for a project (returns basic member info)
    List<com.codesync.project.entity.ProjectMember> getCollaborators(Long projectId);
}