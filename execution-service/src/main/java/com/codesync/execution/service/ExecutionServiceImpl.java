package com.codesync.execution.service;

import com.codesync.execution.dto.ExecutionRequest;
import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.entity.ExecutionJob.JobStatus;
import com.codesync.execution.feign.FileClient;
import com.codesync.execution.feign.ProjectClient;
import com.codesync.execution.repository.ExecutionRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;
    private final ProjectClient projectClient;
    private final FileClient fileClient;
    private final RabbitTemplate rabbitTemplate;

    private static final Map<String, String> LANGUAGE_REGISTRY = Map.of(
            "java", "openjdk:17",
            "python", "python:3.10",
            "javascript", "node:18",
            "node", "node:18",
            "cpp", "gcc:latest",
            "c", "gcc:latest"
    );

    private static final Map<String, String> LANGUAGE_VERSIONS = Map.of(
            "java", "Java 17",
            "python", "Python 3.10",
            "javascript", "Node.js 18",
            "node", "Node.js 18",
            "cpp", "GCC (C++11)",
            "c", "GCC"
    );

    public ExecutionServiceImpl(
            ExecutionRepository executionRepository,
            ProjectClient projectClient,
            FileClient fileClient,
            RabbitTemplate rabbitTemplate) {
        this.executionRepository = executionRepository;
        this.projectClient = projectClient;
        this.fileClient = fileClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public ExecutionJob submitExecution(ExecutionRequest request) {
        ResponseEntity<Object> projectResponse = projectClient.getProject(request.getProjectId());
        if (!projectResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Invalid project ID");
        }

        ResponseEntity<Map<String, String>> fileResponse = fileClient.getFileContent(request.getFileId());
        if (!fileResponse.getStatusCode().is2xxSuccessful() || fileResponse.getBody() == null) {
            throw new IllegalArgumentException("Invalid file ID");
        }

        String sourceCode = fileResponse.getBody().get("content");
        if (sourceCode == null || sourceCode.isEmpty()) {
            throw new IllegalArgumentException("File has no content");
        }

        if (!LANGUAGE_REGISTRY.containsKey(request.getLanguage().toLowerCase())) {
            throw new IllegalArgumentException("Unsupported language: " + request.getLanguage());
        }

        ExecutionJob job = ExecutionJob.builder()
                .jobId(UUID.randomUUID().toString())
                .projectId(request.getProjectId())
                .fileId(request.getFileId())
                .userId(request.getUserId())
                .language(request.getLanguage().toLowerCase())
                .sourceCode(sourceCode)
                .stdin(request.getStdin() != null ? request.getStdin() : "")
                .status(JobStatus.QUEUED)
                .build();

        ExecutionJob savedJob = executionRepository.save(job);

        rabbitTemplate.convertAndSend("execution-queue", savedJob.getJobId());

        return savedJob;
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionJob getJobById(String jobId) {
        return executionRepository.findByJobId(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionJob> getExecutionsByUser(Long userId) {
        return executionRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionJob> getExecutionsByProject(Long projectId) {
        return executionRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public ExecutionJob cancelExecution(String jobId) {
        ExecutionJob job = getJobById(jobId);

        if (job.getStatus() == JobStatus.RUNNING || job.getStatus() == JobStatus.QUEUED) {
            job.setStatus(JobStatus.CANCELLED);
            return executionRepository.save(job);
        }

        throw new IllegalStateException("Cannot cancel job with status: " + job.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionJob getExecutionResult(String jobId) {
        return getJobById(jobId);
    }

    @Override
    public Map<String, String> getSupportedLanguages() {
        return LANGUAGE_VERSIONS;
    }

    @Override
    public String getLanguageVersion(String language) {
        return LANGUAGE_VERSIONS.getOrDefault(language.toLowerCase(), "Unknown");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getExecutionStats(Long userId) {
        List<ExecutionJob> jobs = executionRepository.findByUserId(userId);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) jobs.size());
        stats.put("completed", jobs.stream().filter(j -> j.getStatus() == JobStatus.COMPLETED).count());
        stats.put("failed", jobs.stream().filter(j -> j.getStatus() == JobStatus.FAILED).count());
        stats.put("queued", jobs.stream().filter(j -> j.getStatus() == JobStatus.QUEUED).count());
        stats.put("running", jobs.stream().filter(j -> j.getStatus() == JobStatus.RUNNING).count());

        long avgTime = jobs.stream()
                .filter(j -> j.getExecutionTimeMs() > 0)
                .mapToLong(ExecutionJob::getExecutionTimeMs)
                .sum();
        stats.put("avgExecutionTimeMs", jobs.isEmpty() ? 0 : avgTime / Math.max(1, jobs.size()));

        return stats;
    }

    public Optional<String> getDockerImage(String language) {
        return Optional.ofNullable(LANGUAGE_REGISTRY.get(language.toLowerCase()));
    }
}