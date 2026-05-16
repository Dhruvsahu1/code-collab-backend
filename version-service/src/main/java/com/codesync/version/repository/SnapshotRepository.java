package com.codesync.version.repository;

import com.codesync.version.entity.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {

    List<Snapshot> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Snapshot> findByFileIdOrderByCreatedAtDesc(Long fileId);

    Optional<Snapshot> findByHashAndFileId(String hash, Long fileId);
    
    Optional<Snapshot> findByHash(String hash);

    List<Snapshot> findByProjectIdAndBranchOrderByCreatedAtDesc(Long projectId, String branch);

    List<Snapshot> findByFileIdAndBranchOrderByCreatedAtDesc(Long fileId, String branch);

    Optional<Snapshot> findFirstByFileIdAndBranchOrderByCreatedAtDesc(Long fileId, String branch);

    Optional<Snapshot> findTopByFileIdAndBranchOrderByCreatedAtDesc(Long fileId, String branch);

    Optional<Snapshot> findByProjectIdAndFileIdAndTag(Long projectId, Long fileId, String tag);

    boolean existsByProjectIdAndFileIdAndTag(Long projectId, Long fileId, String tag);

    List<Snapshot> findByFileIdAndBranchAndParentSnapshotId(Long fileId, String branch, Long parentId);
}
