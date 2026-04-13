package com.codesync.version.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSnapshotRequest {
    private Long projectId;
    private Long fileId;
    private Long authorId;
    private String message;
    private String content;
    private String branch;
}