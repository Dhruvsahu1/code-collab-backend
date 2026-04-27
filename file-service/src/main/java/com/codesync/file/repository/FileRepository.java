package com.codesync.file.repository;

import com.codesync.file.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<CodeFile, Long> {

    List<CodeFile> findByProjectId(Long projectId);

    Optional<CodeFile> findByFileId(Long fileId);

    Optional<CodeFile> findByProjectIdAndPath(Long projectId, String path);

    List<CodeFile> findByLanguage(String language);

    List<CodeFile> findByLastEditedBy(Long userId);

    long countByProjectId(Long projectId);

    List<CodeFile> findByIsDeleted(boolean isDeleted);

    void deleteByFileId(Long fileId);

    List<CodeFile> findByProjectIdAndIsDeleted(Long projectId, boolean isDeleted);

    boolean existsByProjectIdAndPath(Long projectId, String path);

    boolean existsByProjectIdAndPathAndIsFolder(Long projectId, String path, boolean isFolder);
    
    @Query("SELECT f FROM CodeFile f WHERE f.projectId = :projectId AND f.isFolder = true AND f.isDeleted = false AND :path LIKE CONCAT(f.path, '%')")
    List<CodeFile> findFoldersInPath(@Param("projectId") Long projectId, @Param("path") String path);
    
    Optional<CodeFile> findByProjectIdAndPathAndIsFolderAndIsDeleted(Long projectId, String path, boolean isFolder, boolean isDeleted);

    @Query("SELECT f FROM CodeFile f WHERE f.projectId = :projectId AND f.isDeleted = false AND f.path LIKE CONCAT(:parentPath, '%') AND f.path != :parentPath")
    List<CodeFile> findByParentPath(@Param("projectId") Long projectId, @Param("parentPath") String parentPath);
}
