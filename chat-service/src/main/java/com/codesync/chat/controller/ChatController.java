package com.codesync.chat.controller;

import com.codesync.chat.entity.ChatMessage;
import com.codesync.chat.service.ChatService;
import com.codesync.chat.util.ChatRoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for chat functionality.
 */
@RestController
@RequestMapping("/chats")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;
    private final ChatRoomManager chatRoomManager;

    public ChatController(ChatService chatService, ChatRoomManager chatRoomManager) {
        this.chatService = chatService;
        this.chatRoomManager = chatRoomManager;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("chat-service is running!");
    }

    /**
     * Get all messages for a project (for initial load / history).
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<List<ChatMessage>> getProjectMessages(@PathVariable Long projectId) {
        List<ChatMessage> messages = chatService.getMessagesByProject(projectId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get recent chat messages for a project.
     */
    @GetMapping("/{projectId}/recent")
    public ResponseEntity<List<ChatMessage>> getRecentMessages(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "50") int size) {
        List<ChatMessage> messages = chatService.getRecentMessages(projectId, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get older messages for infinite scroll.
     */
    @GetMapping("/{projectId}/older")
    public ResponseEntity<List<ChatMessage>> getOlderMessages(
            @PathVariable Long projectId,
            @RequestParam Long timestamp,
            @RequestParam(defaultValue = "20") int size) {
        LocalDateTime beforeTimestamp = LocalDateTime.ofEpochSecond(
                timestamp / 1000, (int) ((timestamp % 1000) * 1000000), java.time.ZoneOffset.UTC);
        List<ChatMessage> messages = chatService.getOlderMessages(projectId, beforeTimestamp, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get paginated messages.
     */
    @GetMapping("/{projectId}/messages")
    public ResponseEntity<Page<ChatMessage>> getProjectMessagesPaginated(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChatMessage> messages = chatService.getProjectMessagesPaginated(projectId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get collaborator count for a project.
     */
    @GetMapping("/{projectId}/collaborator-count")
    public ResponseEntity<Map<String, Integer>> getCollaboratorCount(@PathVariable Long projectId) {
        int count = chatService.getCollaboratorCount(projectId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Get online user count for a project.
     */
    @GetMapping("/{projectId}/online-count")
    public ResponseEntity<Map<String, Object>> getOnlineCount(@PathVariable Long projectId) {
        Set<Long> onlineUsers = chatRoomManager.getOnlineUsers(projectId);
        return ResponseEntity.ok(Map.of(
                "count", onlineUsers.size(),
                "userIds", onlineUsers.stream().toList()
        ));
    }
}