package com.codesync.collab.service;

import com.codesync.collab.dto.*;
import com.codesync.collab.entity.CollabSession;
import com.codesync.collab.entity.Participant;

import java.util.List;
import java.util.Optional;

public interface CollabService {

    CollabSession createSession(CreateSessionRequest request);

    CollabSession getSessionById(String sessionId);

    List<CollabSession> getSessionsByProject(Long projectId);

    Participant joinSession(String sessionId, JoinRequest request);

    void leaveSession(String sessionId, Long userId);

    void endSession(String sessionId);

    List<Participant> getParticipants(String sessionId);

    Participant updateCursor(String sessionId, CursorUpdateRequest request);

    void broadcastChange(String sessionId, CodeChangeRequest request);

    void kickParticipant(String sessionId, Long userId);

    Optional<CollabSession> getActiveSession(Long fileId);
}