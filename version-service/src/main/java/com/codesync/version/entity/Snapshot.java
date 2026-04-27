package com.codesync.version.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "snapshots")
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

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public Long getParentSnapshotId() { return parentSnapshotId; }
    public void setParentSnapshotId(Long parentSnapshotId) { this.parentSnapshotId = parentSnapshotId; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Snapshot)) return false;
        Snapshot snapshot = (Snapshot) o;
        return Objects.equals(snapshotId, snapshot.snapshotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotId);
    }

    @Override
    public String toString() {
        return "Snapshot{" +
            "snapshotId=" + snapshotId +
            ", fileId=" + fileId +
            ", branch='" + branch + '\'' +
            ", tag='" + tag + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}