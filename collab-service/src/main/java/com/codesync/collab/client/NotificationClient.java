package com.codesync.collab.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-client", url = "${services.notification:http://localhost:8089}")
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    Map<String, Object> createNotification(@RequestBody Map<String, Object> notificationRequest);
}