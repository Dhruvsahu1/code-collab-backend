package com.codesync.comment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "version-client", url = "${services.version}")
public interface VersionClient {

    @GetMapping("/versions/file/{fileId}/latest")
    Map<String, Object> getLatestSnapshot(@PathVariable("fileId") Long fileId);
}