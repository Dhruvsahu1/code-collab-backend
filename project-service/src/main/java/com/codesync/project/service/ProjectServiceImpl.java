package com.codesync.project.service;

import com.codesync.project.dto.CreateProjectRequest;
import com.codesync.project.dto.ProjectResponse;
import com.codesync.project.dto.UpdateProjectRequest;
import com.codesync.project.entity.Project;
import com.codesync.project.entity.ProjectMember;
import com.codesync.project.entity.ProjectStar;
import com.codesync.project.enums.Visibility;
import com.codesync.project.exception.DuplicateProjectException;
import com.codesync.project.exception.ProjectNotFoundException;
import com.codesync.project.exception.UnauthorizedAccessException;
import com.codesync.project.feign.AuthClient;
import com.codesync.project.feign.FileServiceClient;
import com.codesync.project.repository.ProjectMemberRepository;
import com.codesync.project.repository.ProjectRepository;
import com.codesync.project.repository.ProjectStarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectStarRepository projectStarRepository;
    private final AuthClient authClient;
    private final FileServiceClient fileServiceClient;

    @Autowired
    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectStarRepository projectStarRepository,
            AuthClient authClient,
            FileServiceClient fileServiceClient) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectStarRepository = projectStarRepository;
        this.authClient = authClient;
        this.fileServiceClient = fileServiceClient;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Long userId) {
        if (request.getOwnerId() == null) {
            request.setOwnerId(userId);
        }
        
        if (!request.getOwnerId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only create projects for yourself");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }

        projectRepository.findByOwnerIdAndName(request.getOwnerId(), request.getName())
            .ifPresent(p -> {
                throw new DuplicateProjectException("Project with name '" + request.getName() + "' already exists");
            });

        Project project = new Project();
        project.setOwnerId(request.getOwnerId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLanguage(request.getLanguage());
        project.setVisibility(request.getVisibilityEnum() != null ? request.getVisibilityEnum() : Visibility.PRIVATE);
        project.setTemplateId(request.getTemplateId());
        project.setStarCount(0);
        project.setForkCount(0);
        project.setArchived(false);

        project = projectRepository.save(project);
        logger.info("Created project: {} by user: {}", project.getProjectId(), userId);
        return mapToResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long userId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (!canAccessProject(project, userId)) {
            throw new UnauthorizedAccessException("You don't have access to this project");
        }

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
                .filter(p -> !p.isArchived())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getPublicProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("starCount").descending());
        return projectRepository.findByVisibility(Visibility.PUBLIC, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("starCount").descending());
        return projectRepository.searchByName(keyword, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByMember(Long userId) {
        return projectMemberRepository.findByUserId(userId).stream()
                .map(member -> projectRepository.findByProjectId(member.getProjectId())
                        .orElseThrow(() -> new ProjectNotFoundException("Project not found")))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByLanguage(String language) {
        return projectRepository.findByLanguage(language).stream()
                .filter(p -> p.getVisibility() == Visibility.PUBLIC && !p.isArchived())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsByLanguage(String language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("starCount").descending());
        return projectRepository.findByLanguageAndVisibility(language, Visibility.PUBLIC, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long userId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (!isOwner(project, userId)) {
            throw new UnauthorizedAccessException("Only the owner can update this project");
        }

        if (request.getName() != null && !request.getName().equals(project.getName())) {
            projectRepository.findByOwnerIdAndName(project.getOwnerId(), request.getName())
                .ifPresent(p -> {
                    throw new DuplicateProjectException("Project with name '" + request.getName() + "' already exists");
                });
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
        logger.info("Updated project: {} by user: {}", projectId, userId);
        return mapToResponse(project);
    }

    @Override
    @Transactional
    public void archiveProject(Long projectId, Long userId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (!isOwner(project, userId)) {
            throw new UnauthorizedAccessException("Only the owner can archive this project");
        }

        project.setArchived(true);
        projectRepository.save(project);
        logger.info("Archived project: {} by user: {}", projectId, userId);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));
        
        if (!isOwner(project, userId)) {
            throw new UnauthorizedAccessException("Only the owner can delete this project");
        }
        
        projectRepository.delete(project);
        logger.info("Deleted project: {} by user: {}", projectId, userId);
    }

    @Override
    @Transactional
    public ProjectResponse forkProject(Long projectId, Long userId) {
        Project original = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (original.getVisibility() == Visibility.PRIVATE && !isOwner(original, userId)) {
            throw new UnauthorizedAccessException("You cannot fork a private project you don't own");
        }

        String forkedName = original.getName() + "-fork";
        int counter = 1;
        while (projectRepository.findByOwnerIdAndName(userId, forkedName).isPresent()) {
            forkedName = original.getName() + "-fork-" + counter++;
        }

        Project forked = new Project();
        forked.setOwnerId(userId);
        forked.setName(forkedName);
        forked.setDescription(original.getDescription());
        forked.setLanguage(original.getLanguage());
        forked.setVisibility(Visibility.PRIVATE);
        forked.setTemplateId(original.getProjectId());
        forked.setStarCount(0);
        forked.setForkCount(0);
        forked.setArchived(false);

        forked = projectRepository.save(forked);

        try {
            fileServiceClient.copyProjectFiles(original.getProjectId(), forked.getProjectId());
        } catch (Exception e) {
            logger.warn("Failed to copy files from source project: {}", e.getMessage());
        }

        original.setForkCount(original.getForkCount() + 1);
        projectRepository.save(original);

        logger.info("Forked project: {} to {} by user: {}", projectId, forked.getProjectId(), userId);
        return mapToResponse(forked);
    }

    @Override
    @Transactional
    public boolean toggleStarProject(Long projectId, Long userId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (!canAccessProject(project, userId)) {
            throw new UnauthorizedAccessException("You don't have access to this project");
        }

        boolean isStarred = projectStarRepository.existsByProjectIdAndUserId(projectId, userId);
        
        if (isStarred) {
            projectStarRepository.deleteByProjectIdAndUserId(projectId, userId);
            project.setStarCount(Math.max(0, project.getStarCount() - 1));
            logger.info("Unstarred project: {} by user: {}", projectId, userId);
        } else {
            ProjectStar star = new ProjectStar(projectId, userId);
            projectStarRepository.save(star);
            project.setStarCount(project.getStarCount() + 1);
            logger.info("Starred project: {} by user: {}", projectId, userId);
        }
        
        projectRepository.save(project);
        return !isStarred;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProjectStarred(Long projectId, Long userId) {
        return projectStarRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getDashboardProjects(Long userId) {
        // 1. Projects owned by user
        List<Project> userProjects = projectRepository.findByOwnerId(userId);
        
        // 2. Projects where user is a collaborator
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);
        List<Project> collabProjects = memberships.stream()
                .map(member -> projectRepository.findByProjectId(member.getProjectId()).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());
        
        // Merge and deduplicate
        java.util.HashSet<Long> addedIds = new java.util.HashSet<>();
        List<ProjectResponse> result = new ArrayList<>();
        
        for (Project p : userProjects) {
            if (!p.isArchived()) {
                result.add(mapToResponse(p));
                addedIds.add(p.getProjectId());
            }
        }
        
        for (Project p : collabProjects) {
            if (!p.isArchived() && !addedIds.contains(p.getProjectId())) {
                result.add(mapToResponse(p));
                addedIds.add(p.getProjectId());
            }
        }
        
        result.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
        
        return result;
    }

    private boolean isOwner(Project project, Long userId) {
        return project.getOwnerId().equals(userId);
    }

    private boolean canAccessProject(Project project, Long userId) {
        if (project.getVisibility() == Visibility.PUBLIC) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        if (isOwner(project, userId)) {
            return true;
        }
        return projectMemberRepository.existsByProjectIdAndUserId(project.getProjectId(), userId);
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

    // Collaborator management methods

    @Override
    @Transactional
    public void addCollaborator(Long projectId, Long userIdToAdd, Long currentUserId) {
        // Check if current user is owner of the project
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (!isOwner(project, currentUserId)) {
            throw new UnauthorizedAccessException("Only the project owner can add collaborators");
        }

        // Check if user to add exists (via auth service)
        // We'll validate the user exists by calling auth service
        // For now, we'll assume the user exists and let the auth service handle validation
        // In a real implementation, we might want to check if the user exists first

        // Check if user is already a collaborator or owner
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userIdToAdd)) {
            throw new IllegalArgumentException("User is already a collaborator or owner of this project");
        }

        // Check if user is the owner (owner cannot be added as collaborator - they're already owner)
        if (project.getOwnerId().equals(userIdToAdd)) {
            throw new IllegalArgumentException("Project owner cannot be added as collaborator");
        }

        // Add the collaborator
        ProjectMember collaborator = new ProjectMember(projectId, userIdToAdd, ProjectMember.ROLE_COLLABORATOR);
        projectMemberRepository.save(collaborator);

        logger.info("Added collaborator {} to project {} by user {}", userIdToAdd, projectId, currentUserId);
    }

    @Override
    @Transactional
    public void removeCollaborator(Long projectId, Long userIdToRemove, Long currentUserId) {
        // Check if current user is owner of the project
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (!isOwner(project, currentUserId)) {
            throw new UnauthorizedAccessException("Only the project owner can remove collaborators");
        }

        // Check if user to remove is the owner (cannot remove owner)
        if (project.getOwnerId().equals(userIdToRemove)) {
            throw new IllegalArgumentException("Cannot remove the project owner");
        }

        // Check if user is actually a collaborator
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userIdToRemove)
                .orElseThrow(() -> new IllegalArgumentException("User is not a collaborator of this project"));

        // Remove the collaborator
        projectMemberRepository.delete(member);

        logger.info("Removed collaborator {} from project {} by user {}", userIdToRemove, projectId, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<ProjectMember> getCollaborators(Long projectId) {
        // Verify project exists
        projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        // Get all members except the owner (we'll get owner separately if needed)
        return projectMemberRepository.findByProjectId(projectId).stream()
                .filter(member -> !member.getRole().equals(ProjectMember.ROLE_OWNER))
                .collect(Collectors.toList());
    }
}
