package com.codesync.comment.config;

import com.codesync.comment.dto.CommentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommentEventService {

    private static final Logger logger = LoggerFactory.getLogger(CommentEventService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocket> sessions = new ConcurrentHashMap<>();
    private HttpClient httpClient = HttpClient.newHttpClient();

    public enum CommentEventType {
        COMMENT_ADDED,
        COMMENT_UPDATED,
        COMMENT_RESOLVED,
        COMMENT_DELETED
    }

    public void broadcastCommentEvent(String sessionId, CommentEventType eventType, CommentResponse comment) {
        try {
            String message = objectMapper.writeValueAsString(java.util.Map.of(
                "type", eventType.name(),
                "sessionId", sessionId,
                "comment", comment
            ));
            
            WebSocket socket = sessions.get(sessionId);
            if (socket != null) {
                socket.sendText(message, true).thenRun(() -> 
                    logger.debug("Sent {} event for comment {}", eventType, comment.getCommentId())
                );
            }
        } catch (Exception e) {
            logger.error("Failed to broadcast comment event: {}", e.getMessage());
        }
    }

    public void registerSession(String sessionId, WebSocket socket) {
        sessions.put(sessionId, socket);
        logger.info("Session {} registered for comment events", sessionId);
    }

    public void unregisterSession(String sessionId) {
        sessions.remove(sessionId);
        logger.info("Session {} unregistered from comment events", sessionId);
    }

    public boolean isSessionActive(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}