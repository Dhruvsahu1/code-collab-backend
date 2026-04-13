package com.codesync.collab.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "project-service", url = "${app.services.project-service}")
public interface ProjectClient {

    @GetMapping("/projects/{id}")
    ResponseEntity<Object> getProject(@PathVariable("id") Long projectId);
}