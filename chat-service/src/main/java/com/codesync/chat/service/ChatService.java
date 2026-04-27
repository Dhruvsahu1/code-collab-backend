package com.codesync.chat.service;

import com.codesync.chat.entity.ChatMessage;
import java.util.List;

public interface ChatService {

    ChatMessage saveMessage(ChatMessage message);

    List<ChatMessage> getMessagesByProject(Long projectId);
}