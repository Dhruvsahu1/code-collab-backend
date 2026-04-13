package com.codesync.execution.controller;

import com.codesync.execution.dto.ExecutionRequest;
import com.codesync.execution.dto.ExecutionResponse;
import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/executions")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping
    public ResponseEntity<ExecutionResponse> submitExecution(@RequestBody ExecutionRequest request) {
        ExecutionJob job = executionService.submitExecution(request);
        return ResponseEntity.ok(ExecutionResponse.fromEntity(job));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ExecutionResponse> getJob(@PathVariable String jobId) {
        ExecutionJob job = executionService.getJobById(jobId);
        return ResponseEntity.ok(ExecutionResponse.fromEntity(job));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExecutionResponse>> getExecutionsByUser(@PathVariable Long userId) {
        List<ExecutionJob> jobs = executionService.getExecutionsByUser(userId);
        List<ExecutionResponse> responses = jobs.stream()
                .map(ExecutionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ExecutionResponse>> getExecutionsByProject(@PathVariable Long projectId) {
        List<ExecutionJob> jobs = executionService.getExecutionsByProject(projectId);
        List<ExecutionResponse> responses = jobs.stream()
                .map(ExecutionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/cancel/{jobId}")
    public ResponseEntity<ExecutionResponse> cancelExecution(@PathVariable String jobId) {
        ExecutionJob job = executionService.cancelExecution(jobId);
        return ResponseEntity.ok(ExecutionResponse.fromEntity(job));
    }

    @GetMapping("/result/{jobId}")
    public ResponseEntity<ExecutionResponse> getExecutionResult(@PathVariable String jobId) {
        ExecutionJob job = executionService.getExecutionResult(jobId);
        return ResponseEntity.ok(ExecutionResponse.fromEntity(job));
    }

    @GetMapping("/languages")
    public ResponseEntity<Map<String, String>> getSupportedLanguages() {
        return ResponseEntity.ok(executionService.getSupportedLanguages());
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<String> getLanguageVersion(@PathVariable String language) {
        return ResponseEntity.ok(executionService.getLanguageVersion(language));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getExecutionStats(@RequestParam Long userId) {
        return ResponseEntity.ok(executionService.getExecutionStats(userId));
    }
}