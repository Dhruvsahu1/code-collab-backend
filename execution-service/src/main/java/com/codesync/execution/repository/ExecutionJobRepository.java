package com.codesync.execution.repository;

import com.codesync.execution.model.ExecutionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExecutionJobRepository extends JpaRepository<ExecutionJob, Long> {
    Optional<ExecutionJob> findByJobId(String jobId);
}