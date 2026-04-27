package com.codesync.chat.repository;

import com.codesync.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByProjectIdOrderByTimestampAsc(Long projectId);

    List<ChatMessage> findBySenderId(Long senderId);
}