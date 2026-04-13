package com.codesync.collab.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${app.services.auth-service}")
public interface AuthClient {

    @GetMapping("/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);
}