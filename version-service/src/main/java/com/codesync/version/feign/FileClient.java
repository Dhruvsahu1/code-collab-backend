package com.codesync.version.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "file-service", url = "${app.services.file-service}")
public interface FileClient {

    @GetMapping("/files/content/{fileId}")
    ResponseEntity<Map<String, String>> getFileContent(@PathVariable("fileId") Long fileId);

    @PutMapping("/files/content/{fileId}")
    ResponseEntity<Object> updateFileContent(
            @PathVariable("fileId") Long fileId,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token);
}