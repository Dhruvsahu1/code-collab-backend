package com.codesync.project.repository;

import com.codesync.project.entity.Project;
import com.codesync.project.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectId(Long projectId);

    Optional<Project> findByOwnerIdAndName(Long ownerId, String name);

    List<Project> findByOwnerId(Long ownerId);

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    List<Project> findByVisibility(Visibility visibility);

    Page<Project> findByVisibility(Visibility visibility, Pageable pageable);

    List<Project> findByLanguage(String language);

    Page<Project> findByLanguage(String language, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.language = :language AND p.visibility = :visibility")
    Page<Project> findByLanguageAndVisibility(@Param("language") String language, @Param("visibility") Visibility visibility, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.visibility = 'PUBLIC' AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Project> searchByName(@Param("keyword") String keyword, Pageable pageable);

    List<Project> findByIsArchived(boolean archived);

    Page<Project> findByIsArchived(boolean archived, Pageable pageable);

    int countByOwnerId(Long ownerId);
}
