package com.codesync.project.repository;

import com.codesync.project.entity.ProjectStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectStarRepository extends JpaRepository<ProjectStar, Long> {
    List<ProjectStar> findByUserId(Long userId);
    List<ProjectStar> findByProjectId(Long projectId);
    Optional<ProjectStar> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
