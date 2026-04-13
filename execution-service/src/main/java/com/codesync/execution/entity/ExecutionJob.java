package com.codesync.execution.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "execution_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionJob {

    @Id
    @Column(name = "job_id", length = 36)
    private String jobId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "language", nullable = false, length = 20)
    private String language;

    @Lob
    @Column(name = "source_code", columnDefinition = "TEXT")
    private String sourceCode;

    @Lob
    @Column(name = "stdin", columnDefinition = "TEXT")
    private String stdin;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Lob
    @Column(name = "stdout", columnDefinition = "TEXT")
    private String stdout;

    @Lob
    @Column(name = "stderr", columnDefinition = "TEXT")
    private String stderr;

    @Column(name = "exit_code")
    private int exitCode;

    @Column(name = "execution_time_ms")
    private long executionTimeMs;

    @Column(name = "memory_used_kb")
    private long memoryUsedKb;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum JobStatus {
        QUEUED,
        RUNNING,
        COMPLETED,
        FAILED,
        TIMED_OUT,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = JobStatus.QUEUED;
        }
    }
}