package com.codesync.version.dto;

import com.codesync.version.entity.Snapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotResponse {
    private Long snapshotId;
    private Long projectId;
    private Long fileId;
    private Long authorId;
    private String message;
    private String hash;
    private Long parentSnapshotId;
    private String branch;
    private String tag;
    private LocalDateTime createdAt;

    public static SnapshotResponse fromEntity(Snapshot snapshot) {
        return SnapshotResponse.builder()
                .snapshotId(snapshot.getSnapshotId())
                .projectId(snapshot.getProjectId())
                .fileId(snapshot.getFileId())
                .authorId(snapshot.getAuthorId())
                .message(snapshot.getMessage())
                .hash(snapshot.getHash())
                .parentSnapshotId(snapshot.getParentSnapshotId())
                .branch(snapshot.getBranch())
                .tag(snapshot.getTag())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}