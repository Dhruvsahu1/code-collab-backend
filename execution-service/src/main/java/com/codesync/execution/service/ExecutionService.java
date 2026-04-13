package com.codesync.execution.service;

import com.codesync.execution.dto.ExecutionRequest;
import com.codesync.execution.dto.ExecutionResponse;
import com.codesync.execution.entity.ExecutionJob;

import java.util.List;
import java.util.Map;

public interface ExecutionService {

    ExecutionJob submitExecution(ExecutionRequest request);

    ExecutionJob getJobById(String jobId);

    List<ExecutionJob> getExecutionsByUser(Long userId);

    List<ExecutionJob> getExecutionsByProject(Long projectId);

    ExecutionJob cancelExecution(String jobId);

    ExecutionJob getExecutionResult(String jobId);

    Map<String, String> getSupportedLanguages();

    String getLanguageVersion(String language);

    Map<String, Long> getExecutionStats(Long userId);
}