package com.codesync.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    private Long projectId;
    private Long fileId;
    private Long authorId;
    private String content;
    private Integer lineNumber;
    private Integer columnNumber;
    private Long parentCommentId;
    private Long snapshotId;
}