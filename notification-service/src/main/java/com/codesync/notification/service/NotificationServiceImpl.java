package com.codesync.notification.service;

import com.codesync.notification.dto.NotificationRequest;
import com.codesync.notification.dto.NotificationResponse;
import com.codesync.notification.dto.UnreadCountResponse;
import com.codesync.notification.entity.Notification;
import com.codesync.notification.entity.Notification.NotificationType;
import com.codesync.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   SimpMessagingTemplate messagingTemplate,
                                   EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) {
        log.info("Sending notification to user {} with type {}", request.getRecipientId(), request.getType());

        Notification notification = new Notification();
        notification.setRecipientId(request.getRecipientId());
        notification.setActorId(request.getActorId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setRelatedId(request.getRelatedId());
        notification.setRelatedType(request.getRelatedType());
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification saved with id: {}", saved.getNotificationId());

        NotificationResponse response = NotificationResponse.fromEntity(saved);

        pushWebSocketNotification(saved);

        if (Boolean.TRUE.equals(request.getSendEmail())) {
            sendEmailAsync(saved);
        }

        return response;
    }

    @Override
    public List<NotificationResponse> sendBulk(List<NotificationRequest> requests) {
        log.info("Sending bulk notifications: {} requests", requests.size());

        List<Notification> notifications = requests.stream()
                .map(request -> {
                    Notification n = new Notification();
                    n.setRecipientId(request.getRecipientId());
                    n.setActorId(request.getActorId());
                    n.setType(request.getType());
                    n.setTitle(request.getTitle());
                    n.setMessage(request.getMessage());
                    n.setRelatedId(request.getRelatedId());
                    n.setRelatedType(request.getRelatedType());
                    n.setIsRead(false);
                    return n;
                })
                .collect(Collectors.toList());

        List<Notification> saved = notificationRepository.saveAll(notifications);

        for (Notification n : saved) {
            pushWebSocketNotification(n);
        }

        return saved.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);

        pushWebSocketNotification(saved);

        return NotificationResponse.fromEntity(saved);
    }

    @Override
    public void markAllRead(Long recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false);

        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);

        log.info("Marked all notifications as read for user: {}", recipientId);

        Notification dummy = new Notification();
        dummy.setRecipientId(recipientId);
        pushWebSocketNotification(dummy);
    }

    @Override
    public void deleteRead(Long recipientId) {
        List<Notification> read = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, true);
        notificationRepository.deleteAll(read);

        log.info("Deleted {} read notifications for user: {}", read.size(), recipientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteByNotificationId(notificationId);
        log.info("Deleted notification: {}", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll()
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByType(NotificationType type) {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByRelatedId(Long relatedId) {
        return notificationRepository.findByRelatedIdOrderByCreatedAtDesc(relatedId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void pushWebSocketNotification(Notification notification) {
        try {
            Long recipientId = notification.getRecipientId();
            Long unreadCount = getUnreadCount(recipientId);

            String destination = "/topic/notifications/" + recipientId;

            messagingTemplate.convertAndSend(destination, java.util.Map.of(
                    "notification", NotificationResponse.fromEntity(notification),
                    "unreadCount", unreadCount
            ));

            log.debug("Pushed WebSocket notification to user {} at {}", recipientId, destination);
        } catch (Exception e) {
            log.warn("Failed to push WebSocket notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendEmailAsync(Notification notification) {
        try {
            emailService.sendNotificationEmail(notification);
        } catch (Exception e) {
            log.warn("Failed to send email notification: {}", e.getMessage());
        }
    }
}