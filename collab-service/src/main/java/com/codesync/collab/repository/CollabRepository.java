package com.codesync.collab.repository;

import com.codesync.collab.entity.CollabSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollabRepository extends JpaRepository<CollabSession, String> {

    Optional<CollabSession> findBySessionId(String sessionId);

    List<CollabSession> findByProjectId(Long projectId);

    List<CollabSession> findByFileId(Long fileId);

    List<CollabSession> findByProjectIdAndStatus(Long projectId, CollabSession.SessionStatus status);

    List<CollabSession> findByOwnerId(Long ownerId);
}