package com.codesync.project.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "file-service", url = "${app.services.file-service}")
public interface FileServiceClient {

    @PostMapping("/api/files/copy")
    void copyProjectFiles(
            @RequestParam("sourceProjectId") Long sourceProjectId,
            @RequestParam("targetProjectId") Long targetProjectId
    );
}
