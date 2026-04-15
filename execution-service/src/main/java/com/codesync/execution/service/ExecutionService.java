package com.codesync.execution.service;

import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.repository.ExecutionJobRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExecutionService {

    private final ExecutionJobRepository jobRepository;

    public ExecutionService(ExecutionJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public ExecutionJob createJob(Long userId, Long fileId, String language, String code) {
        ExecutionJob job = new ExecutionJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setUserId(userId);
        job.setFileId(fileId);
        job.setLanguage(language);
        job.setCode(code);
        job.setStatus("RUNNING");
        return jobRepository.save(job);
    }

    public Optional<ExecutionJob> getJob(String jobId) {
        return jobRepository.findByJobId(jobId);
    }

    public ExecutionJob executeCode(String jobId, String output, String error) {
        ExecutionJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        if (error != null && !error.isEmpty()) {
            job.setStatus("ERROR");
            job.setError(error);
        } else {
            job.setStatus("SUCCESS");
            job.setOutput(output);
        }
        job.setCompletedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public void cancelJob(String jobId) {
        jobRepository.findByJobId(jobId).ifPresent(job -> {
            job.setStatus("CANCELLED");
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }
}