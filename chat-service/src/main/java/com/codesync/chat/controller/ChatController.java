package com.codesync.chat.controller;

import com.codesync.chat.entity.ChatMessage;
import com.codesync.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long projectId) {
        logger.info("API HIT: GET /chats/{}", projectId);
        return ResponseEntity.ok(chatService.getMessagesByProject(projectId));
    }
}