package com.codesync.collab.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "file-client", url = "${services.file:http://localhost:8083}")
public interface FileClient {

    @GetMapping("/files/{fileId}")
    ResponseEntity<Map<String, Object>> getFile(@PathVariable("fileId") Long fileId);

    @GetMapping("/files/content/{fileId}")
    ResponseEntity<Map<String, String>> getFileContent(@PathVariable("fileId") Long fileId);

    @PutMapping("/files/content/{fileId}")
    ResponseEntity<Map<String, Object>> updateContent(
        @PathVariable("fileId") Long fileId,
        @RequestBody Map<String, String> content
    );
}