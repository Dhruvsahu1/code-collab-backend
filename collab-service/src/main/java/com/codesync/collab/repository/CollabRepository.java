package com.codesync.collab.repository;

import com.codesync.collab.model.CollabSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollabRepository extends JpaRepository<CollabSession, Long> {

    Optional<CollabSession> findBySessionId(String sessionId);

    List<CollabSession> findByProjectId(Long projectId);

    List<CollabSession> findByFileId(Long fileId);

    List<CollabSession> findByProjectIdAndStatus(Long projectId, CollabSession.SessionStatus status);

    List<CollabSession> findByOwnerId(Long ownerId);

    List<CollabSession> findByStatusAndUpdatedAtBefore(
        CollabSession.SessionStatus status, LocalDateTime cutoff);

    @Query("SELECT c FROM CollabSession c WHERE c.fileId = :fileId AND c.status = 'ACTIVE'")
    Optional<CollabSession> findActiveByFileId(@Param("fileId") Long fileId);

    @Query("SELECT c FROM CollabSession c WHERE c.projectId = :projectId AND c.status = 'ACTIVE'")
    Optional<CollabSession> findActiveByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.session.sessionId = :sessionId AND p.isActive = true")
    long countParticipants(@Param("sessionId") String sessionId);
}
