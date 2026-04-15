package com.codesync.collab.service;

import com.codesync.collab.model.CollabSession;
import com.codesync.collab.repository.CollabSessionRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class CollabService {
    private final CollabSessionRepository sessionRepository;

    public CollabService(CollabSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public CollabSession createSession(Long fileId, Long ownerId) {
        CollabSession session = new CollabSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setFileId(fileId);
        session.setOwnerId(ownerId);
        session.setCode("");
        session.setIsActive(true);
        return sessionRepository.save(session);
    }

    public Optional<CollabSession> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }

    public Optional<CollabSession> getActiveSession(Long fileId) {
        return sessionRepository.findByFileIdAndIsActiveTrue(fileId);
    }

    public CollabSession updateCode(String sessionId, String code) {
        CollabSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setCode(code);
        return sessionRepository.save(session);
    }

    public void closeSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setIsActive(false);
            sessionRepository.save(session);
        });
    }
}