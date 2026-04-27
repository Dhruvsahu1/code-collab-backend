package com.codesync.collab.controller;

import com.codesync.collab.dto.JoinSessionRequest;
import com.codesync.collab.dto.UpdateCursorRequest;
import com.codesync.collab.model.CollabSession;
import com.codesync.collab.model.Participant;
import com.codesync.collab.service.CollabService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class CollabWebSocketController {

    private final CollabService collabService;
    private final SimpMessagingTemplate messagingTemplate;

    public CollabWebSocketController(CollabService collabService, SimpMessagingTemplate messagingTemplate) {
        this.collabService = collabService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/code.change")
    public void handleCodeChange(Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        String code = (String) payload.get("code");
        Object userIdObj = payload.get("userId");
        String userId = userIdObj != null ? String.valueOf(userIdObj) : "unknown";
        
        if (sessionId != null && code != null) {
            collabService.updateCode(sessionId, code);
            CollabSession session = collabService.getSessionById(sessionId).orElse(null);
            int version = session != null && session.getVersion() != null ? session.getVersion() : 0;
            
            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/changes", Map.of(
                "type", "CODE_CHANGE",
                "sessionId", sessionId,
                "code", code,
                "userId", userId,
                "version", version,
                "timestamp", System.currentTimeMillis()
            ));
            
            messagingTemplate.convertAndSend("/topic/session/" + sessionId, Map.of(
                "type", "CODE_CHANGE",
                "sessionId", sessionId,
                "code", code,
                "userId", userId,
                "version", version,
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @MessageMapping("/session.end")
    public void endSession(Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        if (sessionId != null) {
            collabService.endSession(sessionId);
            messagingTemplate.convertAndSend("/topic/session/" + sessionId, Map.of(
                "type", "SESSION_ENDED",
                "sessionId", sessionId
            ));
        }
    }

    @MessageMapping("/session/{sessionId}/cursor")
    public void handleCursorUpdate(
            @DestinationVariable String sessionId,
            Map<String, Object> payload) {
        try {
            UpdateCursorRequest request = new UpdateCursorRequest();
            request.setUserId(Long.valueOf(payload.get("userId").toString()));
            request.setCursorLine(Integer.valueOf(payload.get("cursorLine").toString()));
            request.setCursorCol(Integer.valueOf(payload.get("cursorCol").toString()));
            collabService.updateCursor(sessionId, request);

            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/cursor", Map.of(
                    "type", "CURSOR_UPDATE",
                    "sessionId", sessionId,
                    "userId", request.getUserId(),
                    "cursorLine", request.getCursorLine(),
                    "cursorCol", request.getCursorCol(),
                    "color", payload.get("color")
            ));
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/cursor", Map.of(
                    "type", "CURSOR_UPDATE",
                    "sessionId", sessionId,
                    "userId", payload.get("userId"),
                    "cursorLine", payload.get("cursorLine"),
                    "cursorCol", payload.get("cursorCol"),
                    "color", payload.get("color")
            ));
        }
    }

    @MessageMapping("/session/{sessionId}/join")
    @SendTo("/topic/session/{sessionId}/participants")
    public Map<String, Object> handleJoin(
            @DestinationVariable String sessionId,
            Map<String, Object> payload) {
        try {
            JoinSessionRequest request = new JoinSessionRequest();
            request.setUserId(Long.valueOf(payload.get("userId").toString()));
            if (payload.get("role") != null) {
                request.setRole(com.codesync.collab.model.Participant.ParticipantRole.valueOf(payload.get("role").toString()));
            }
            if (payload.get("sessionPassword") != null) {
                request.setSessionPassword(payload.get("sessionPassword").toString());
            }
            Participant participant = collabService.joinSession(sessionId, request);
            return Map.of(
                    "type", "PARTICIPANT_JOINED",
                    "sessionId", sessionId,
                    "userId", participant.getUserId(),
                    "color", participant.getColor(),
                    "role", participant.getRole().name()
            );
        } catch (Exception e) {
            return Map.of(
                    "type", "JOIN_ERROR",
                    "sessionId", sessionId,
                    "error", e.getMessage()
            );
        }
    }

    @MessageMapping("/session/{sessionId}/leave")
    @SendTo("/topic/session/{sessionId}/participants")
    public Map<String, Object> handleLeave(
            @DestinationVariable String sessionId,
            Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            collabService.leaveSession(sessionId, userId);
            return Map.of(
                    "type", "PARTICIPANT_LEFT",
                    "sessionId", sessionId,
                    "userId", userId
            );
        } catch (Exception e) {
            return Map.of(
                    "type", "LEAVE_ERROR",
                    "sessionId", sessionId,
                    "error", e.getMessage()
            );
        }
    }
}