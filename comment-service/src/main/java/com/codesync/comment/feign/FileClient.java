package com.codesync.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "fileClient", url = "${services.file}")
public interface FileClient {

    @GetMapping("/files/{id}")
    ResponseEntity<Object> getFile(@PathVariable("id") Long fileId);
}