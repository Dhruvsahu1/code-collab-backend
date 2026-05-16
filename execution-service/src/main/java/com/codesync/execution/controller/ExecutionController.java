package com.codesync.execution.controller;

import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.service.ExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/executions")
public class ExecutionController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionController.class);

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping({"/submit", "/run"})
    public ResponseEntity<ExecutionJob> submitExecution(@RequestBody Map<String, Object> request) {
        logger.info("Received execution request: language={}, code length={}", 
            request.get("language"), 
            request.get("code") != null ? request.get("code").toString().length() : 0);

        Long userId = request.get("userId") != null ? Long.parseLong(request.get("userId").toString()) : 1L;
        Long fileId = request.get("fileId") != null ? Long.parseLong(request.get("fileId").toString()) : 1L;
        String language = request.get("language") != null ? request.get("language").toString() : "javascript";
        String code = request.get("code") != null ? request.get("code").toString() : "";
        String stdin = request.get("stdin") != null ? request.get("stdin").toString() : null;

        ExecutionJob job = executionService.submitExecution(userId, fileId, language, code, stdin);
        
        logger.info("Job submitted: jobId={}, status={}", job.getJobId(), job.getStatus());
        return ResponseEntity.ok(job);
    }

    @GetMapping({"/submit", "/run"})
    public ResponseEntity<Map<String, String>> getSubmitInfo() {
        return ResponseEntity.ok(Map.of(
            "message", "Use POST to submit code for execution",
            "example", "{\"language\": \"javascript\", \"code\": \"console.log('Hello')\"}"
        ));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ExecutionJob> getJob(@PathVariable String jobId) {
        return executionService.getJob(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ExecutionJob> getJobAlt(@PathVariable String jobId) {
        return executionService.getJob(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/result/{jobId}")
    public ResponseEntity<ExecutionService.ExecutionResult> getResult(@PathVariable String jobId) {
        ExecutionService.ExecutionResult result = executionService.getResult(jobId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cancel/{jobId}")
    public ResponseEntity<ExecutionJob> cancelJob(@PathVariable String jobId) {
        try {
            ExecutionJob job = executionService.cancelJob(jobId);
            return ResponseEntity.ok(job);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> getLanguages() {
        return ResponseEntity.ok(executionService.getSupportedLanguages());
    }

    @GetMapping("/stats")
    public ResponseEntity<ExecutionService.ExecutionStats> getStats() {
        return ResponseEntity.ok(executionService.getStats());
    }
}