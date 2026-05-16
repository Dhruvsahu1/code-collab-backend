package com.codesync.chat.config;

import com.codesync.chat.util.ChatRoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles WebSocket connect/disconnect events for chat.
 * Cleans up online/typing state when users disconnect unexpectedly.
 */
@Component
public class ChatWebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketEventListener.class);

    private final ChatRoomManager chatRoomManager;
    private final SimpMessagingTemplate messagingTemplate;

    // Track which session is in which project: wsSessionId -> projectId
    private final Map<String, Long> sessionProjectMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsernameMap = new ConcurrentHashMap<>();

    public ChatWebSocketEventListener(ChatRoomManager chatRoomManager,
                                      SimpMessagingTemplate messagingTemplate) {
        this.chatRoomManager = chatRoomManager;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String wsSessionId = accessor.getSessionId();
        log.info("Chat WS connection established: {}", wsSessionId);
    }

    /**
     * Register a user's WebSocket session with a project.
     * Called from the join handler.
     */
    public void registerSession(String wsSessionId, Long projectId, Long userId, String username) {
        sessionProjectMap.put(wsSessionId, projectId);
        sessionUserMap.put(wsSessionId, userId);
        sessionUsernameMap.put(wsSessionId, username != null ? username : "User " + userId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String wsSessionId = accessor.getSessionId();

        Long projectId = sessionProjectMap.remove(wsSessionId);
        Long userId = sessionUserMap.remove(wsSessionId);
        String username = sessionUsernameMap.remove(wsSessionId);

        if (projectId != null && userId != null) {
            log.info("Chat WS disconnected: user {} from project {}", userId, projectId);

            chatRoomManager.removeOnlineUser(projectId, userId);
            chatRoomManager.removeTypingUser(projectId, userId);

            // Broadcast that user went offline
            messagingTemplate.convertAndSend("/topic/chat/" + projectId, Map.of(
                    "type", "LEAVE",
                    "projectId", projectId,
                    "senderId", userId,
                    "senderUsername", username != null ? username : "User " + userId,
                    "content", (username != null ? username : "User " + userId) + " disconnected",
                    "messageType", "LEAVE",
                    "timestamp", LocalDateTime.now().toString(),
                    "onlineUsers", chatRoomManager.getOnlineUsers(projectId).stream().toList()
            ));
        } else {
            log.debug("Chat WS disconnected: {} (no project mapping)", wsSessionId);
        }
    }
}
