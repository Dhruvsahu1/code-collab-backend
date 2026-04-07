package com.codesync.project.service;

import com.codesync.project.dto.CreateProjectRequest;
import com.codesync.project.dto.ProjectResponse;
import com.codesync.project.dto.UpdateProjectRequest;
import com.codesync.project.entity.Project;
import com.codesync.project.enums.Visibility;
import com.codesync.project.feign.AuthClient;
import com.codesync.project.feign.FileClient;
import com.codesync.project.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AuthClient authClient;
    private final FileClient fileClient;

    public ProjectServiceImpl(ProjectRepository projectRepository, AuthClient authClient, FileClient fileClient) {
        this.projectRepository = projectRepository;
        this.authClient = authClient;
        this.fileClient = fileClient;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        if (request.getOwnerId() == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        
        Project project = new Project();
        project.setOwnerId(request.getOwnerId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLanguage(request.getLanguage());
        project.setVisibility(request.getVisibility() != null ? request.getVisibility() : Visibility.PRIVATE);
        project.setTemplateId(request.getTemplateId());
        project.setStarCount(0);
        project.setForkCount(0);
        project.setArchived(false);

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        return mapToResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsByOwner(Long ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return projectRepository.findByOwnerId(ownerId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getPublicProjects() {
        return projectRepository.findByVisibility(Visibility.PUBLIC).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getPublicProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return projectRepository.findByVisibility(Visibility.PUBLIC, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return projectRepository.searchByName(keyword, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByMember(Long userId) {
        // To be implemented with ProjectMember entity
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByLanguage(String language) {
        return projectRepository.findByLanguage(language).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsByLanguage(String language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("starCount").descending());
        return projectRepository.findByLanguage(language, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getLanguage() != null) {
            project.setLanguage(request.getLanguage());
        }
        if (request.getVisibility() != null) {
            project.setVisibility(request.getVisibility());
        }

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Override
    @Transactional
    public void archiveProject(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        project.setArchived(true);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectResponse forkProject(Long projectId, Long newOwnerId) {
        Project original = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        Project forked = new Project();
        forked.setOwnerId(newOwnerId);
        forked.setName(original.getName() + "-fork");
        forked.setDescription(original.getDescription());
        forked.setLanguage(original.getLanguage());
        forked.setVisibility(Visibility.PRIVATE);
        forked.setTemplateId(original.getProjectId());
        forked.setStarCount(0);
        forked.setForkCount(0);
        forked.setArchived(false);

        forked = projectRepository.save(forked);

        try {
            fileClient.copyProjectFiles(original.getProjectId(), forked.getProjectId());
        } catch (Exception e) {
            // Log but continue - file copy can be done async
        }

        original.setForkCount(original.getForkCount() + 1);
        projectRepository.save(original);

        return mapToResponse(forked);
    }

    @Override
    @Transactional
    public void starProject(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        project.setStarCount(project.getStarCount() + 1);
        projectRepository.save(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .ownerId(project.getOwnerId())
                .name(project.getName())
                .description(project.getDescription())
                .language(project.getLanguage())
                .visibility(project.getVisibility())
                .templateId(project.getTemplateId())
                .archived(project.isArchived())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .starCount(project.getStarCount())
                .forkCount(project.getForkCount())
                .build();
    }
}