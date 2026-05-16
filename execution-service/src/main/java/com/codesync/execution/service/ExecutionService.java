package com.codesync.execution.service;

import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.repository.ExecutionJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionService.class);

    private final ExecutionJobRepository jobRepository;
    private final AsyncJobProcessor jobProcessor;

    public ExecutionService(ExecutionJobRepository jobRepository, AsyncJobProcessor jobProcessor) {
        this.jobRepository = jobRepository;
        this.jobProcessor = jobProcessor;
    }

    public ExecutionJob submitExecution(Long userId, Long fileId, String language, String code, String stdin) {
        ExecutionJob job = new ExecutionJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setUserId(userId != null ? userId : 1L);
        job.setFileId(fileId != null ? fileId : 1L);
        job.setLanguage(normalizeLanguage(language));
        job.setCode(code);
        job.setStatus("QUEUED");
        job.setStartedAt(LocalDateTime.now());
        
        job = jobRepository.save(job);
        logger.info("Created job {} with status QUEUED", job.getJobId());

        jobProcessor.submitJob(job);
        logger.info("Submitted job {} for async processing", job.getJobId());
        
        return job;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "javascript";
        }
        
        String lang = language.toLowerCase().trim();
        
        Map<String, String> aliases = Map.of(
            "js", "javascript",
            "node", "javascript",
            "py", "python",
            "python3", "python",
            "ts", "typescript",
            "c++", "cpp",
            "cs", "csharp"
        );
        
        return aliases.getOrDefault(lang, lang);
    }

    public Optional<ExecutionJob> getJob(String jobId) {
        return jobRepository.findByJobId(jobId);
    }

    public ExecutionJob cancelJob(String jobId) {
        Optional<ExecutionJob> optJob = jobRepository.findByJobId(jobId);
        if (optJob.isEmpty()) {
            throw new RuntimeException("Job not found: " + jobId);
        }
        
        ExecutionJob job = optJob.get();
        if ("QUEUED".equals(job.getStatus()) || "RUNNING".equals(job.getStatus())) {
            job.setStatus("CANCELLED");
            job.setCompletedAt(LocalDateTime.now());
            job = jobRepository.save(job);
            logger.info("Job {} cancelled", jobId);
        }
        
        return job;
    }

    public ExecutionResult getResult(String jobId) {
        return jobRepository.findByJobId(jobId)
            .map(job -> new ExecutionResult(
                job.getStatus(),
                job.getStdout(),
                job.getStderr(),
                job.getError(),
                job.getExitCode(),
                job.getExecutionTimeMs(),
                job.getStartedAt(),
                job.getCompletedAt()
            ))
            .orElse(null);
    }

    public List<String> getSupportedLanguages() {
        return List.of("javascript", "python", "java", "cpp", "c", "typescript", "go", "rust", "ruby", "php", "csharp");
    }

    public ExecutionStats getStats() {
        List<ExecutionJob> allJobs = jobRepository.findAll();
        
        long total = allJobs.size();
        long queued = allJobs.stream().filter(j -> "QUEUED".equals(j.getStatus())).count();
        long running = allJobs.stream().filter(j -> "RUNNING".equals(j.getStatus())).count();
        long completed = allJobs.stream().filter(j -> "COMPLETED".equals(j.getStatus())).count();
        long failed = allJobs.stream().filter(j -> "FAILED".equals(j.getStatus())).count();
        long timeout = allJobs.stream().filter(j -> "TIMEOUT".equals(j.getStatus())).count();
        
        long avgTime = (long) allJobs.stream()
            .filter(j -> j.getExecutionTimeMs() != null)
            .mapToLong(ExecutionJob::getExecutionTimeMs)
            .average()
            .orElse(0.0);
        
        return new ExecutionStats(total, queued, running, completed, failed, timeout, avgTime);
    }

    public static class ExecutionResult {
        private final String status;
        private final String stdout;
        private final String stderr;
        private final String error;
        private final Integer exitCode;
        private final Long executionTimeMs;
        private final LocalDateTime startedAt;
        private final LocalDateTime completedAt;

        public ExecutionResult(String status, String stdout, String stderr, String error, 
                               Integer exitCode, Long executionTimeMs, 
                               LocalDateTime startedAt, LocalDateTime completedAt) {
            this.status = status;
            this.stdout = stdout;
            this.stderr = stderr;
            this.error = error;
            this.exitCode = exitCode;
            this.executionTimeMs = executionTimeMs;
            this.startedAt = startedAt;
            this.completedAt = completedAt;
        }

        public String getStatus() { return status; }
        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
        public String getError() { return error; }
        public Integer getExitCode() { return exitCode; }
        public Long getExecutionTimeMs() { return executionTimeMs; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }

    public static class ExecutionStats {
        private final long totalJobs;
        private final long queued;
        private final long running;
        private final long completed;
        private final long failed;
        private final long timeout;
        private final long avgExecutionTimeMs;

        public ExecutionStats(long totalJobs, long queued, long running, long completed, 
                              long failed, long timeout, long avgExecutionTimeMs) {
            this.totalJobs = totalJobs;
            this.queued = queued;
            this.running = running;
            this.completed = completed;
            this.failed = failed;
            this.timeout = timeout;
            this.avgExecutionTimeMs = avgExecutionTimeMs;
        }

        public long getTotalJobs() { return totalJobs; }
        public long getQueued() { return queued; }
        public long getRunning() { return running; }
        public long getCompleted() { return completed; }
        public long getFailed() { return failed; }
        public long getTimeout() { return timeout; }
        public long getAvgExecutionTimeMs() { return avgExecutionTimeMs; }
    }
}