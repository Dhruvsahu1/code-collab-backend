package com.codesync.notification.service;

import com.codesync.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:noreply@codesync.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendNotificationEmail(Notification notification) {
        if (!emailEnabled) {
            log.debug("Email disabled, skipping notification email");
            return;
        }

        if (notification.getRecipientId() == null) {
            log.warn("Cannot send email: no recipient ID");
            return;
        }

        try {
            String subject = buildSubject(notification);
            String body = buildBody(notification);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(getUserEmail(notification.getRecipientId()));
            message.setFrom(fromEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to user {} for notification type {}", 
                    notification.getRecipientId(), notification.getType());
        } catch (Exception e) {
            log.warn("Failed to send email notification: {}", e.getMessage());
        }
    }

    private String buildSubject(Notification notification) {
        return "CodeSync: " + notification.getTitle();
    }

    private String buildBody(Notification notification) {
        StringBuilder body = new StringBuilder();
        body.append(notification.getMessage()).append("\n\n");
        body.append("Type: ").append(notification.getType()).append("\n");
        
        if (notification.getRelatedId() != null) {
            body.append("Related ID: ").append(notification.getRelatedId()).append("\n");
        }
        
        if (notification.getRelatedType() != null) {
            body.append("Related Type: ").append(notification.getRelatedType()).append("\n");
        }
        
        body.append("\n---\n");
        body.append("CodeSync Notification System");
        
        return body.toString();
    }

    private String getUserEmail(Long userId) {
        return "user" + userId + "@codesync.local";
    }
}