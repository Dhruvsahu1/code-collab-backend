package com.codesync.execution.repository;

import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.entity.ExecutionJob.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutionRepository extends JpaRepository<ExecutionJob, String> {

    Optional<ExecutionJob> findByJobId(String jobId);

    List<ExecutionJob> findByUserId(Long userId);

    List<ExecutionJob> findByProjectId(Long projectId);

    List<ExecutionJob> findByStatus(JobStatus status);

    List<ExecutionJob> findByLanguage(String language);

    List<ExecutionJob> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByUserId(Long userId);
}