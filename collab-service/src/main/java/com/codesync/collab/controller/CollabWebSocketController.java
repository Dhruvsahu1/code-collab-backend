package com.codesync.collab.controller;

import com.codesync.collab.dto.CodeChangeMessage;
import com.codesync.collab.dto.CursorMessage;
import com.codesync.collab.dto.ParticipantMessage;
import com.codesync.collab.model.CollabSession;
import com.codesync.collab.service.CollabService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * WebSocket controller for realtime collaboration.
 * 
 * Unified topic structure:
 *   /topic/session/{sessionId}/code         - code changes
 *   /topic/session/{sessionId}/cursors      - cursor positions
 *   /topic/session/{sessionId}/participants - join/leave events
 *   /topic/session/{sessionId}/state        - full state sync (on join)
 */
@Controller
public class CollabWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(CollabWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final CollabService collabService;

    // Debounced save: sessionId -> latest code content
    private final ConcurrentHashMap<String, String> pendingSaves = new ConcurrentHashMap<>();
    private final ScheduledExecutorService saveScheduler = Executors.newSingleThreadScheduledExecutor();

    // Version tracking per session (in-memory, resets on restart)
    private final ConcurrentHashMap<String, Long> sessionVersions = new ConcurrentHashMap<>();

    // Map of sessionId -> Map of userId -> Participant data (for syncing late joiners)
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, Map<String, Object>>> activeSessionsParticipants = new ConcurrentHashMap<>();

    public CollabWebSocketController(SimpMessagingTemplate messagingTemplate, CollabService collabService) {
        this.messagingTemplate = messagingTemplate;
        this.collabService = collabService;

        // Run debounced save every 3 seconds
        saveScheduler.scheduleAtFixedRate(this::flushPendingSaves, 3, 3, TimeUnit.SECONDS);
    }

    /**
     * Handle code change from a client.
     * Client sends to: /app/session.change
     * Broadcasts to: /topic/session/{sessionId}/code (excluding sender)
     */
    @MessageMapping("/session.change")
    public void handleCodeChange(@Payload CodeChangeMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = message.getSessionId();
        Long userId = message.getUserId();
        log.debug("Code change for session {} from user {}", sessionId, userId);

        // Update version
        long newVersion = sessionVersions.merge(sessionId, 1L, Long::sum);

        // Queue debounced save
        pendingSaves.put(sessionId, message.getContent());

        // Broadcast to all subscribers (frontend filters out own userId)
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "CODE_CHANGE");
        payload.put("sessionId", sessionId);
        payload.put("userId", userId);
        payload.put("fileId", message.getFileId());
        payload.put("content", message.getContent());
        payload.put("version", newVersion);
        payload.put("timestamp", message.getTimestamp());

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/code", payload);
    }

    /**
     * Handle cursor update.
     * Client sends to: /app/session.cursor
     * Broadcasts to: /topic/session/{sessionId}/cursors
     */
    @MessageMapping("/session.cursor")
    public void handleCursorUpdate(@Payload CursorMessage message) {
        log.trace("Cursor update: user {} in session {} at {}:{}", 
                message.getUserId(), message.getSessionId(), message.getLine(), message.getColumn());

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "CURSOR_UPDATE");
        payload.put("sessionId", message.getSessionId());
        payload.put("userId", message.getUserId());
        payload.put("line", message.getLine());
        payload.put("column", message.getColumn());
        payload.put("color", message.getColor());
        payload.put("timestamp", message.getTimestamp());

        messagingTemplate.convertAndSend("/topic/session/" + message.getSessionId() + "/cursors", payload);
    }

    /**
     * Handle user join.
     * Client sends to: /app/session.join
     * Broadcasts USER_JOINED to: /topic/session/{sessionId}/participants
     * Sends full state to the joining user via: /topic/session/{sessionId}/state
     */
    @MessageMapping("/session.join")
    public void handleJoin(@Payload ParticipantMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = message.getSessionId();
        Long userId = message.getUserId();
        String username = message.getUsername();

        log.info("User {} ({}) joining session {}", userId, username, sessionId);

        // Store session mapping for disconnect handling
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("collabSessionId", sessionId);
            headerAccessor.getSessionAttributes().put("collabUserId", userId);
            headerAccessor.getSessionAttributes().put("collabUsername", username);
        }

        // Add to in-memory active participants for state syncing
        Map<String, Object> participantInfo = new HashMap<>();
        participantInfo.put("userId", userId);
        participantInfo.put("username", username != null ? username : "User " + userId);
        participantInfo.put("color", message.getColor());
        participantInfo.put("role", message.getRole());
        
        activeSessionsParticipants
            .computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
            .put(userId, participantInfo);

        // Broadcast join event to all participants
        Map<String, Object> joinPayload = new HashMap<>();
        joinPayload.put("type", "USER_JOINED");
        joinPayload.put("sessionId", sessionId);
        joinPayload.put("userId", userId);
        joinPayload.put("username", username != null ? username : "User " + userId);
        joinPayload.put("action", "JOIN");
        joinPayload.put("color", message.getColor());
        joinPayload.put("role", message.getRole());
        joinPayload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/participants", joinPayload);

        // Send current session state to the joining user
        sendSessionState(sessionId);
    }

    /**
     * Handle user leave (voluntary).
     * Client sends to: /app/session.leave
     * Broadcasts USER_LEFT to: /topic/session/{sessionId}/participants
     */
    @MessageMapping("/session.leave")
    public void handleLeave(@Payload ParticipantMessage message) {
        String sessionId = message.getSessionId();
        Long userId = message.getUserId();

        log.info("User {} leaving session {}", userId, sessionId);

        // Remove from in-memory active participants
        ConcurrentHashMap<Long, Map<String, Object>> sessionParts = activeSessionsParticipants.get(sessionId);
        if (sessionParts != null) {
            sessionParts.remove(userId);
            if (sessionParts.isEmpty()) {
                activeSessionsParticipants.remove(sessionId);
            }
        }

        Map<String, Object> leavePayload = new HashMap<>();
        leavePayload.put("type", "USER_LEFT");
        leavePayload.put("sessionId", sessionId);
        leavePayload.put("userId", userId);
        leavePayload.put("username", message.getUsername() != null ? message.getUsername() : "User " + userId);
        leavePayload.put("action", "LEAVE");
        leavePayload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/participants", leavePayload);

        // Flush any pending saves for this session
        flushSessionSave(sessionId);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Send full session state to the /state topic (for late-joiners to sync).
     */
    private void sendSessionState(String sessionId) {
        try {
            Optional<CollabSession> sessionOpt = collabService.getSessionById(sessionId);
            if (sessionOpt.isPresent()) {
                CollabSession session = sessionOpt.get();
                long currentVersion = sessionVersions.getOrDefault(sessionId, (long) session.getVersion());

                // Check if we have a more recent in-memory version
                String latestCode = pendingSaves.getOrDefault(sessionId, session.getCode());

                Map<String, Object> statePayload = new HashMap<>();
                statePayload.put("type", "SESSION_STATE");
                statePayload.put("sessionId", sessionId);
                statePayload.put("code", latestCode != null ? latestCode : "");
                statePayload.put("version", currentVersion);
                statePayload.put("language", session.getLanguage());
                statePayload.put("projectId", session.getProjectId());
                statePayload.put("fileId", session.getFileId());
                statePayload.put("projectName", session.getProjectName());
                statePayload.put("fileName", session.getFileName());
                statePayload.put("timestamp", LocalDateTime.now().toString());

                // Attach current active participants
                ConcurrentHashMap<Long, Map<String, Object>> parts = activeSessionsParticipants.get(sessionId);
                if (parts != null) {
                    statePayload.put("participants", parts.values());
                } else {
                    statePayload.put("participants", new java.util.ArrayList<>());
                }

                messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/state", statePayload);
            }
        } catch (Exception e) {
            log.error("Failed to send session state for {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Flush all pending saves to database.
     */
    private void flushPendingSaves() {
        pendingSaves.forEach((sessionId, code) -> {
            try {
                pendingSaves.remove(sessionId);
                collabService.updateCode(sessionId, code);
                log.debug("Debounced save completed for session {}", sessionId);
            } catch (Exception e) {
                log.error("Failed to save code for session {}: {}", sessionId, e.getMessage());
            }
        });
    }

    /**
     * Flush pending save for a specific session (on leave/disconnect).
     */
    public void flushSessionSave(String sessionId) {
        String code = pendingSaves.remove(sessionId);
        if (code != null) {
            try {
                collabService.updateCode(sessionId, code);
                log.info("Flushed pending save for session {} on leave", sessionId);
            } catch (Exception e) {
                log.error("Failed to flush save for session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    /**
     * Get pending saves map (used by WebSocketEventListener for cleanup).
     */
    public ConcurrentHashMap<String, String> getPendingSaves() {
        return pendingSaves;
    }

    /**
     * Broadcast a participant left event (called from WebSocketEventListener).
     */
    public void broadcastUserLeft(String sessionId, Long userId, String username) {
        // Remove from in-memory active participants
        ConcurrentHashMap<Long, Map<String, Object>> sessionParts = activeSessionsParticipants.get(sessionId);
        if (sessionParts != null) {
            sessionParts.remove(userId);
            if (sessionParts.isEmpty()) {
                activeSessionsParticipants.remove(sessionId);
            }
        }

        Map<String, Object> leavePayload = new HashMap<>();
        leavePayload.put("type", "USER_LEFT");
        leavePayload.put("sessionId", sessionId);
        leavePayload.put("userId", userId);
        leavePayload.put("username", username != null ? username : "User " + userId);
        leavePayload.put("action", "DISCONNECT");
        leavePayload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/participants", leavePayload);
    }
}