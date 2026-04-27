package com.codesync.chat.controller;

import com.codesync.chat.dto.ChatMessageDTO;
import com.codesync.chat.entity.ChatMessage;
import com.codesync.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/chat/{projectId}")
    public ChatMessage sendMessage(ChatMessageDTO dto, @DestinationVariable Long projectId) {
        ChatMessage message = new ChatMessage();
        message.setProjectId(projectId);
        message.setSenderId(dto.getSenderId());
        message.setSenderUsername(dto.getSenderUsername());
        message.setContent(dto.getContent());
        message.setMessageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT");
        message.setTimestamp(LocalDateTime.now());
        
        chatService.saveMessage(message);
        return message;
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/chat/{projectId}")
    public ChatMessage joinProject(ChatMessageDTO dto, @DestinationVariable Long projectId) {
        ChatMessage message = new ChatMessage();
        message.setProjectId(projectId);
        message.setSenderId(dto.getSenderId());
        message.setSenderUsername(dto.getSenderUsername());
        message.setContent(dto.getSenderUsername() + " joined the project");
        message.setMessageType("JOIN");
        message.setTimestamp(LocalDateTime.now());
        
        chatService.saveMessage(message);
        return message;
    }
}