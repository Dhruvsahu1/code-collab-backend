package com.codesync.project.service;

import com.codesync.project.dto.CreateProjectRequest;
import com.codesync.project.dto.ProjectResponse;
import com.codesync.project.dto.UpdateProjectRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProjectById(Long projectId);

    List<ProjectResponse> getProjectsByOwner(Long ownerId);

    Page<ProjectResponse> getProjectsByOwner(Long ownerId, int page, int size);

    List<ProjectResponse> getPublicProjects();

    Page<ProjectResponse> getPublicProjects(int page, int size);

    Page<ProjectResponse> searchProjects(String keyword, int page, int size);

    List<ProjectResponse> getProjectsByMember(Long userId);

    List<ProjectResponse> getProjectsByLanguage(String language);

    Page<ProjectResponse> getProjectsByLanguage(String language, int page, int size);

    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request);

    void archiveProject(Long projectId);

    void deleteProject(Long projectId);

    ProjectResponse forkProject(Long projectId, Long newOwnerId);

    void starProject(Long projectId);
}