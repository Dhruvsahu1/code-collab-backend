package com.codesync.execution.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "projectClient", url = "${services.project}")
public interface ProjectClient {

    @GetMapping("/projects/{id}")
    ResponseEntity<Object> getProject(@PathVariable("id") Long projectId);
}