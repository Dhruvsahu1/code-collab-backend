package com.codesync.collab.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "file-service", url = "${app.services.file-service}")
public interface FileClient {

    @GetMapping("/files/{id}")
    ResponseEntity<Object> getFile(@PathVariable("id") Long fileId);
}