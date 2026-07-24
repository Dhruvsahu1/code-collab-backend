package com.codesync.collab.service;

import com.codesync.collab.dto.CollabSessionRequest;
import com.codesync.collab.dto.JoinSessionRequest;
import com.codesync.collab.dto.UpdateCursorRequest;
import com.codesync.collab.model.CollabSession;
import com.codesync.collab.model.Participant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CollabService {
    CollabSession createSession(CollabSessionRequest request);
    Optional<CollabSession> getSessionById(String sessionId);
    List<CollabSession> getSessionsByProject(Long projectId);
    List<CollabSession> getSessionsByFile(Long fileId);
    List<CollabSession> getSessionsByOwner(Long ownerId);
    Optional<CollabSession> getActiveSession(Long fileId);
    Optional<CollabSession> getActiveSessionByProject(Long projectId);
    CollabSession updateCode(String sessionId, String code);
    void endSession(String sessionId, Long requesterId);

    Participant joinSession(String sessionId, JoinSessionRequest request);
    void leaveSession(String sessionId, Long userId);
    void kickParticipant(String sessionId, Long requesterId, Long targetUserId);
    List<Participant> getParticipants(String sessionId);
    Participant updateCursor(String sessionId, UpdateCursorRequest request);

    void broadcastChange(String sessionId, Object payload);
    void broadcastEvent(String sessionId, Map<String, Object> event);
}
