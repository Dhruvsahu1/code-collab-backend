package com.codesync.collab.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "project-client", url = "${services.project:http://localhost:8082}")
public interface ProjectClient {

    @GetMapping("/projects/{projectId}")
    ResponseEntity<Map<String, Object>> getProject(@PathVariable("projectId") Long projectId);
}