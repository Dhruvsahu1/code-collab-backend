package com.codesync.execution.service;

import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.repository.ExecutionJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.*;

@Service
public class AsyncJobProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncJobProcessor.class);
    private static final int THREAD_POOL_SIZE = 4;

    private final ExecutionJobRepository jobRepository;
    private final DockerExecutor dockerExecutor;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExecutorService executorService;

    public AsyncJobProcessor(ExecutionJobRepository jobRepository, DockerExecutor dockerExecutor, 
                            SimpMessagingTemplate messagingTemplate) {
        this.jobRepository = jobRepository;
        this.dockerExecutor = dockerExecutor;
        this.messagingTemplate = messagingTemplate;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @PostConstruct
    public void init() {
        logger.info("AsyncJobProcessor initialized with thread pool size: {}", THREAD_POOL_SIZE);
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down AsyncJobProcessor");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void submitJob(ExecutionJob job) {
        logger.info("Submitting job {} for async processing", job.getJobId());
        
        executorService.submit(() -> processJob(job));
    }

    private void processJob(ExecutionJob job) {
        logger.info("Processing job: {}", job.getJobId());
        
        try {
            job.setStatus("RUNNING");
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);

            messagingTemplate.convertAndSend("/topic/execution/" + job.getJobId(), 
                new ExecutionOutput("RUNNING", "Starting execution..."));

            StringBuilder stdoutCapture = new StringBuilder();
            StringBuilder stderrCapture = new StringBuilder();

            DockerExecutor.DockerResult result = dockerExecutor.execute(
                job.getLanguage(),
                job.getCode(),
                null,
                (line, isError) -> {
                    if (isError) {
                        stderrCapture.append(line).append("\n");
                    } else {
                        stdoutCapture.append(line).append("\n");
                    }
                    messagingTemplate.convertAndSend("/topic/execution/" + job.getJobId(),
                        new ExecutionOutput(isError ? "stderr" : "stdout", line));
                }
            );

            job.setStdout(stdoutCapture.toString());
            job.setStderr(stderrCapture.toString());
            job.setExitCode(result.getExitCode());
            job.setExecutionTimeMs(result.getExecutionTimeMs());

            if (result.isTimedOut()) {
                job.setStatus("TIMEOUT");
                job.setError(result.getError());
            } else if (result.getExitCode() != 0) {
                job.setStatus("FAILED");
                String err = result.getError() != null ? result.getError() : stderrCapture.toString();
                job.setError(err.isEmpty() ? "Execution failed with non-zero exit code" : err);
                job.setOutput(stdoutCapture.toString());
            } else {
                job.setStatus("COMPLETED");
                job.setOutput(stdoutCapture.toString());
            }

            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            messagingTemplate.convertAndSend("/topic/execution/" + job.getJobId(),
                new ExecutionOutput("status", job.getStatus()));

            logger.info("Job {} completed with status: {}", job.getJobId(), job.getStatus());
            
        } catch (Exception e) {
            logger.error("Error processing job " + job.getJobId(), e);
            job.setStatus("ERROR");
            job.setError("Processing error: " + e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            messagingTemplate.convertAndSend("/topic/execution/" + job.getJobId(),
                new ExecutionOutput("error", e.getMessage()));
        }
    }
}