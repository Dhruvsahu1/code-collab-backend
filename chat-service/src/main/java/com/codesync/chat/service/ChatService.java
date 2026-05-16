package com.codesync.chat.service;

import com.codesync.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat service interface.
 */
public interface ChatService {

    ChatMessage saveMessage(ChatMessage message);

    List<ChatMessage> getMessagesByProject(Long projectId);

    List<ChatMessage> getRecentMessages(Long projectId, int size);

    List<ChatMessage> getOlderMessages(Long projectId, LocalDateTime beforeTimestamp, int size);

    Page<ChatMessage> getProjectMessagesPaginated(Long projectId, int page, int size);

    ChatMessage sendSystemMessage(Long projectId, Long userId, String username, String content, String messageType);

    boolean isCollaborator(Long projectId, Long userId);

    int getCollaboratorCount(Long projectId);
}