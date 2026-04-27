package com.codesync.notification.repository;

import com.codesync.notification.entity.Notification;
import com.codesync.notification.entity.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, Boolean isRead);

    long countByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    List<Notification> findByRelatedIdOrderByCreatedAtDesc(Long relatedId);

    Optional<Notification> findByNotificationId(Long notificationId);

    void deleteByNotificationId(Long notificationId);

    void deleteByRecipientIdAndIsRead(Long recipientId, Boolean isRead);
}