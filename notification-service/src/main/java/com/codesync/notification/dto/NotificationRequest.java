package com.codesync.notification.dto;

import com.codesync.notification.entity.Notification.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class NotificationRequest {

    @NotNull
    private Long recipientId;

    private Long actorId;

    @NotNull
    private NotificationType type;

    @NotNull
    private String title;

    @NotNull
    private String message;

    private Long relatedId;

    private String relatedType;

    private Boolean sendEmail = false;
}