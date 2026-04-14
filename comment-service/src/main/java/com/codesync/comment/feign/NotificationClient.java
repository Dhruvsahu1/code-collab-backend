package com.codesync.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notificationClient", url = "${services.notification}")
public interface NotificationClient {

    @PostMapping("/notifications/mention")
    ResponseEntity<Void> sendMentionNotification(@RequestBody Map<String, Object> notification);
}