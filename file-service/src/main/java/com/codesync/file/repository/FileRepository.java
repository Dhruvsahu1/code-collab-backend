package com.codesync.file.repository;

import com.codesync.file.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
