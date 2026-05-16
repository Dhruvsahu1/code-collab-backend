package com.codesync.chat.controller;

import com.codesync.chat.dto.ChatMessageDTO;
import com.codesync.chat.entity.ChatMessage;
import com.codesync.chat.service.ChatService;
import com.codesync.chat.util.ChatRoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket controller for handling chat messages and events.
 * Uses SimpMessagingTemplate for manual routing instead of broken @SendTo + @DestinationVariable.
 */
@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;
    private final ChatRoomManager chatRoomManager;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService,
                                   ChatRoomManager chatRoomManager,
                                   SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.chatRoomManager = chatRoomManager;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send a chat message.
     * Client sends to: /app/chat.sendMessage
     * Broadcasts to: /topic/chat/{projectId}
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO dto,
                            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserId(headerAccessor, dto.getSenderId());
        String username = getUsername(headerAccessor, dto.getSenderUsername());
        Long projectId = dto.getProjectId();

        log.info("Chat message from user {} in project {}: {}", userId, projectId, dto.getContent());

        ChatMessage message = new ChatMessage();
        message.setProjectId(projectId);
        message.setSenderId(userId);
        message.setSenderUsername(username);
        message.setContent(dto.getContent());
        message.setMessageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT");
        message.setTimestamp(LocalDateTime.now());

        // Persist
        ChatMessage saved = chatService.saveMessage(message);

        // Broadcast to all subscribers of this project's chat topic
        messagingTemplate.convertAndSend("/topic/chat/" + projectId, Map.of(
                "type", "MESSAGE",
                "messageId", saved.getMessageId(),
                "projectId", projectId,
                "senderId", userId,
                "senderUsername", username != null ? username : "User " + userId,
                "content", dto.getContent(),
                "messageType", saved.getMessageType(),
                "timestamp", saved.getTimestamp().toString()
        ));
    }

    /**
     * User joins a project chat room.
     */
    @MessageMapping("/chat.join")
    public void joinProject(@Payload ChatMessageDTO dto,
                            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserId(headerAccessor, dto.getSenderId());
        String username = getUsername(headerAccessor, dto.getSenderUsername());
        Long projectId = dto.getProjectId();

        log.info("User {} ({}) joining chat for project {}", userId, username, projectId);

        // Track online status
        chatRoomManager.addOnlineUser(projectId, userId);

        // Persist join message
        ChatMessage message = chatService.sendSystemMessage(
                projectId, userId, username,
                (username != null ? username : "User " + userId) + " joined the chat",
                "JOIN");

        // Broadcast join event
        messagingTemplate.convertAndSend("/topic/chat/" + projectId, Map.of(
                "type", "JOIN",
                "projectId", projectId,
                "senderId", userId,
                "senderUsername", username != null ? username : "User " + userId,
                "content", message.getContent(),
                "messageType", "JOIN",
                "timestamp", message.getTimestamp().toString(),
                "onlineUsers", chatRoomManager.getOnlineUsers(projectId).stream().toList()
        ));
    }

    /**
     * User leaves a project chat room.
     */
    @MessageMapping("/chat.leave")
    public void leaveProject(@Payload ChatMessageDTO dto,
                             SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserId(headerAccessor, dto.getSenderId());
        String username = getUsername(headerAccessor, dto.getSenderUsername());
        Long projectId = dto.getProjectId();

        log.info("User {} leaving chat for project {}", userId, projectId);

        chatRoomManager.removeOnlineUser(projectId, userId);
        chatRoomManager.removeTypingUser(projectId, userId);

        ChatMessage message = chatService.sendSystemMessage(
                projectId, userId, username,
                (username != null ? username : "User " + userId) + " left the chat",
                "LEAVE");

        messagingTemplate.convertAndSend("/topic/chat/" + projectId, Map.of(
                "type", "LEAVE",
                "projectId", projectId,
                "senderId", userId,
                "senderUsername", username != null ? username : "User " + userId,
                "content", message.getContent(),
                "messageType", "LEAVE",
                "timestamp", message.getTimestamp().toString(),
                "onlineUsers", chatRoomManager.getOnlineUsers(projectId).stream().toList()
        ));
    }

    /**
     * User starts typing.
     */
    @MessageMapping("/chat.typingStart")
    public void typingStart(@Payload ChatMessageDTO dto,
                            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserId(headerAccessor, dto.getSenderId());
        String username = getUsername(headerAccessor, dto.getSenderUsername());
        Long projectId = dto.getProjectId();

        chatRoomManager.addTypingUser(projectId, userId);

        messagingTemplate.convertAndSend("/topic/chat/" + projectId, Map.of(
                "type", "TYPING_START",
                "projectId", projectId,
                "senderId", userId,
                "senderUsername", username != null ? username : "User " + userId,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * User stops typing.
     */
    @MessageMapping("/chat.typingStop")
    public void typingStop(@Payload ChatMessageDTO dto,
                           SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getUserId(headerAccessor, dto.getSenderId());
        String username = getUsername(headerAccessor, dto.getSenderUsername());
        Long projectId = dto.getProjectId();

        chatRoomManager.removeTypingUser(projectId, userId);

        messagingTemplate.convertAndSend("/topic/chat/" + projectId, Map.of(
                "type", "TYPING_STOP",
                "projectId", projectId,
                "senderId", userId,
                "senderUsername", username != null ? username : "User " + userId,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long getUserId(SimpMessageHeaderAccessor accessor, Long fallback) {
        if (accessor.getSessionAttributes() != null) {
            Object userId = accessor.getSessionAttributes().get("userId");
            if (userId instanceof Long) return (Long) userId;
            if (userId instanceof Number) return ((Number) userId).longValue();
        }
        return fallback != null ? fallback : 0L;
    }

    private String getUsername(SimpMessageHeaderAccessor accessor, String fallback) {
        if (accessor.getSessionAttributes() != null) {
            Object username = accessor.getSessionAttributes().get("username");
            if (username instanceof String) return (String) username;
        }
        return fallback;
    }
}