package com.codesync.version.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long snapshotId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "message")
    private String message;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "hash", length = 64)
    private String hash;

    @Column(name = "parent_snapshot_id")
    private Long parentSnapshotId;

    @Column(name = "branch")
    @Builder.Default
    private String branch = "main";

    @Column(name = "tag")
    private String tag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}