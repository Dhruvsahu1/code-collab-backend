package com.codesync.notification.service;

import com.codesync.notification.dto.NotificationRequest;
import com.codesync.notification.dto.NotificationResponse;
import com.codesync.notification.entity.Notification.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationResponse send(NotificationRequest request);

    List<NotificationResponse> sendBulk(List<NotificationRequest> requests);

    NotificationResponse markAsRead(Long notificationId);

    void markAllRead(Long recipientId);

    void deleteRead(Long recipientId);

    List<NotificationResponse> getByRecipient(Long recipientId);

    Long getUnreadCount(Long recipientId);

    void deleteNotification(Long notificationId);

    List<NotificationResponse> getAll();

    List<NotificationResponse> getByType(NotificationType type);

    List<NotificationResponse> getByRelatedId(Long relatedId);
}