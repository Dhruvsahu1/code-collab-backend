package com.codesync.comment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "collab-client", url = "${services.collab}")
public interface CollabClient {

    @PostMapping("/api/sessions/broadcast")
    Map<String, Object> broadcastEvent(@RequestBody Map<String, Object> event);
}