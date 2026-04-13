package com.codesync.execution.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "fileClient", url = "${services.file}")
public interface FileClient {

    @GetMapping("/files/{id}")
    ResponseEntity<Object> getFile(@PathVariable("id") Long fileId);

    @GetMapping("/files/content/{fileId}")
    ResponseEntity<Map<String, String>> getFileContent(@PathVariable("fileId") Long fileId);
}