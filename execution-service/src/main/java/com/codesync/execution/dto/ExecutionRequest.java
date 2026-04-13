package com.codesync.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {
    private Long projectId;
    private Long fileId;
    private Long userId;
    private String language;
    private String stdin;
}