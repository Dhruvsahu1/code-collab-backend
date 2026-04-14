package com.codesync.comment.dto;

import com.codesync.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long commentId;
    private Long projectId;
    private Long fileId;
    private Long authorId;
    private String content;
    private Integer lineNumber;
    private Integer columnNumber;
    private Long parentCommentId;
    private Boolean resolved;
    private Long snapshotId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .projectId(comment.getProjectId())
                .fileId(comment.getFileId())
                .authorId(comment.getAuthorId())
                .content(comment.getContent())
                .lineNumber(comment.getLineNumber())
                .columnNumber(comment.getColumnNumber())
                .parentCommentId(comment.getParentCommentId())
                .resolved(comment.getResolved())
                .snapshotId(comment.getSnapshotId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}