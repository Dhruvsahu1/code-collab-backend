package com.codesync.chat.service;

import com.codesync.chat.client.ProjectServiceClient;
import com.codesync.chat.entity.ChatMessage;
import com.codesync.chat.repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatRepository chatRepository;
    private final ProjectServiceClient projectServiceClient;

    public ChatServiceImpl(ChatRepository chatRepository,
                           ProjectServiceClient projectServiceClient) {
        this.chatRepository = chatRepository;
        this.projectServiceClient = projectServiceClient;
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        log.debug("Saving message from user {} in project {}", message.getSenderId(), message.getProjectId());
        return chatRepository.save(message);
    }

    @Override
    public List<ChatMessage> getMessagesByProject(Long projectId) {
        return chatRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }

    @Override
    public List<ChatMessage> getRecentMessages(Long projectId, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("timestamp").descending());
        List<ChatMessage> messages = chatRepository.findByProjectIdOrderByTimestampDesc(projectId, pageable).getContent();
        // Reverse so oldest is first for display
        List<ChatMessage> reversed = new java.util.ArrayList<>(messages);
        java.util.Collections.reverse(reversed);
        return reversed;
    }

    @Override
    public List<ChatMessage> getOlderMessages(Long projectId, LocalDateTime beforeTimestamp, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("timestamp").descending());
        return chatRepository.findByProjectIdAndTimestampLessThanOrderByTimestampDesc(
                projectId, beforeTimestamp, pageable).getContent();
    }

    @Override
    public Page<ChatMessage> getProjectMessagesPaginated(Long projectId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return chatRepository.findByProjectIdOrderByTimestampDesc(projectId, pageable);
    }

    @Override
    public ChatMessage sendSystemMessage(Long projectId, Long userId, String username, String content, String messageType) {
        ChatMessage message = new ChatMessage();
        message.setProjectId(projectId);
        message.setSenderId(userId);
        message.setSenderUsername(username);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setTimestamp(LocalDateTime.now());
        return chatRepository.save(message);
    }

    @Override
    public boolean isCollaborator(Long projectId, Long userId) {
        return projectServiceClient.isCollaborator(projectId, userId);
    }

    @Override
    public int getCollaboratorCount(Long projectId) {
        return projectServiceClient.getCollaboratorCount(projectId);
    }
}