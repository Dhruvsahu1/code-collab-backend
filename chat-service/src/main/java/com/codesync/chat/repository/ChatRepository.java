package com.codesync.chat.repository;

import com.codesync.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByProjectIdOrderByTimestampAsc(Long projectId);

    List<ChatMessage> findBySenderId(Long senderId);

    // New methods for pagination
    Page<ChatMessage> findByProjectIdOrderByTimestampDesc(Long projectId, Pageable pageable);
    Page<ChatMessage> findByProjectIdAndTimestampLessThanOrderByTimestampDesc(Long projectId, LocalDateTime timestamp, Pageable pageable);
}