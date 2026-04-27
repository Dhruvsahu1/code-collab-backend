package com.codesync.chat.service;

import com.codesync.chat.entity.ChatMessage;
import com.codesync.chat.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        return chatRepository.save(message);
    }

    @Override
    public List<ChatMessage> getMessagesByProject(Long projectId) {
        return chatRepository.findByProjectIdOrderByTimestampAsc(projectId);
    }
}