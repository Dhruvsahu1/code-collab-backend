package com.codesync.execution.dto;

import com.codesync.execution.entity.ExecutionJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {
    private String jobId;
    private Long projectId;
    private Long fileId;
    private Long userId;
    private String language;
    private String status;
    private String stdout;
    private String stderr;
    private int exitCode;
    private long executionTimeMs;
    private long memoryUsedKb;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static ExecutionResponse fromEntity(ExecutionJob job) {
        return ExecutionResponse.builder()
                .jobId(job.getJobId())
                .projectId(job.getProjectId())
                .fileId(job.getFileId())
                .userId(job.getUserId())
                .language(job.getLanguage())
                .status(job.getStatus().name())
                .stdout(job.getStdout())
                .stderr(job.getStderr())
                .exitCode(job.getExitCode())
                .executionTimeMs(job.getExecutionTimeMs())
                .memoryUsedKb(job.getMemoryUsedKb())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}