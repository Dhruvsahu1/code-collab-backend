package com.codesync.notification.controller;

import com.codesync.notification.dto.NotificationRequest;
import com.codesync.notification.dto.NotificationResponse;
import com.codesync.notification.dto.UnreadCountResponse;
import com.codesync.notification.entity.Notification.NotificationType;
import com.codesync.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.send(request));
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<List<NotificationResponse>> sendBulk(@Valid @RequestBody List<NotificationRequest> requests) {
        return ResponseEntity.ok(notificationService.sendBulk(requests));
    }

    @GetMapping("/{recipientId}")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(@PathVariable Long recipientId) {
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId));
    }

    @GetMapping("/unread-count/{recipientId}")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable Long recipientId) {
        Long count = notificationService.getUnreadCount(recipientId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all/{recipientId}")
    public ResponseEntity<Void> markAllRead(@PathVariable Long recipientId) {
        notificationService.markAllRead(recipientId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/read/{recipientId}")
    public ResponseEntity<Void> deleteRead(@PathVariable Long recipientId) {
        notificationService.deleteRead(recipientId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationResponse>> getByType(@PathVariable NotificationType type) {
        return ResponseEntity.ok(notificationService.getByType(type));
    }

    @GetMapping("/related/{relatedId}")
    public ResponseEntity<List<NotificationResponse>> getByRelatedId(@PathVariable Long relatedId) {
        return ResponseEntity.ok(notificationService.getByRelatedId(relatedId));
    }
}