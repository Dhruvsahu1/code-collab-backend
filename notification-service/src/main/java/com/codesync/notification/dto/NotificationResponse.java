package com.codesync.notification.dto;

import com.codesync.notification.entity.Notification;
import com.codesync.notification.entity.Notification.NotificationType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationResponse {

    private Long notificationId;
    private Long recipientId;
    private Long actorId;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(notification.getNotificationId());
        response.setRecipientId(notification.getRecipientId());
        response.setActorId(notification.getActorId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setRelatedId(notification.getRelatedId());
        response.setRelatedType(notification.getRelatedType());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}