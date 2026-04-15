package com.codesync.execution.controller;

import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/execution")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/run")
    public ResponseEntity<ExecutionJob> runCode(@RequestBody Map<String, Object> request) {
        ExecutionJob job = executionService.createJob(
                Long.parseLong(request.get("userId").toString()),
                Long.parseLong(request.get("fileId").toString()),
                request.get("language").toString(),
                request.get("code").toString()
        );
        return ResponseEntity.ok(job);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ExecutionJob> getJob(@PathVariable String jobId) {
        return executionService.getJob(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/jobs/{jobId}/complete")
    public ResponseEntity<ExecutionJob> completeJob(
            @PathVariable String jobId,
            @RequestBody Map<String, String> payload) {
        ExecutionJob job = executionService.executeCode(jobId, payload.get("output"), payload.get("error"));
        return ResponseEntity.ok(job);
    }

    @PostMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<Void> cancelJob(@PathVariable String jobId) {
        executionService.cancelJob(jobId);
        return ResponseEntity.ok().build();
    }
}