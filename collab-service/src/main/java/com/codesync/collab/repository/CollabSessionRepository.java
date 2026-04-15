package com.codesync.collab.repository;

import com.codesync.collab.model.CollabSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CollabSessionRepository extends JpaRepository<CollabSession, Long> {
    Optional<CollabSession> findBySessionId(String sessionId);
    Optional<CollabSession> findByFileIdAndIsActiveTrue(Long fileId);
}
