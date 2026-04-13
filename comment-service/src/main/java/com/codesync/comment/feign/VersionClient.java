package com.codesync.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "versionClient", url = "${services.version}")
public interface VersionClient {

    @GetMapping("/versions/{id}")
    ResponseEntity<Object> getSnapshot(@PathVariable("id") Long snapshotId);
}