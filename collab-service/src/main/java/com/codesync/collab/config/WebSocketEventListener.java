package com.codesync.collab.config;

import com.codesync.collab.controller.CollabWebSocketController;
import com.codesync.collab.model.Participant;
import com.codesync.collab.repository.ParticipantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Handles WebSocket connect/disconnect lifecycle events for collaboration sessions.
 * Ensures participants are properly cleaned up on unexpected disconnects.
 */
@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final ParticipantRepository participantRepository;
    private final CollabWebSocketController collabWebSocketController;

    public WebSocketEventListener(ParticipantRepository participantRepository,
                                   CollabWebSocketController collabWebSocketController) {
        this.participantRepository = participantRepository;
        this.collabWebSocketController = collabWebSocketController;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Collab WS connection: session={}", accessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String wsSessionId = accessor.getSessionId();

        if (accessor.getSessionAttributes() == null) {
            log.debug("Collab WS disconnect: {} (no session attributes)", wsSessionId);
            return;
        }

        Object collabSessionIdObj = accessor.getSessionAttributes().get("collabSessionId");
        Object collabUserIdObj = accessor.getSessionAttributes().get("collabUserId");
        Object collabUsernameObj = accessor.getSessionAttributes().get("collabUsername");

        if (collabSessionIdObj == null || collabUserIdObj == null) {
            log.debug("Collab WS disconnect: {} (not in a collab session)", wsSessionId);
            return;
        }

        String sessionId = collabSessionIdObj.toString();
        Long userId;
        try {
            userId = Long.parseLong(collabUserIdObj.toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid userId in session attributes: {}", collabUserIdObj);
            return;
        }
        String username = collabUsernameObj != null ? collabUsernameObj.toString() : "User " + userId;

        log.info("Collab WS disconnect: user {} ({}) from session {}", userId, username, sessionId);

        // Mark participant as inactive in the database
        try {
            Optional<Participant> participantOpt = participantRepository
                    .findBySession_SessionIdAndUserId(sessionId, userId);
            if (participantOpt.isPresent()) {
                Participant participant = participantOpt.get();
                if (Boolean.TRUE.equals(participant.getIsActive())) {
                    participant.setIsActive(false);
                    participant.setLeftAt(LocalDateTime.now());
                    participantRepository.save(participant);
                    log.info("Marked participant {} as inactive in session {}", userId, sessionId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update participant status on disconnect: {}", e.getMessage());
        }

        // Flush any pending code saves for this session
        collabWebSocketController.flushSessionSave(sessionId);

        // Broadcast user left to remaining participants
        collabWebSocketController.broadcastUserLeft(sessionId, userId, username);
    }
}
