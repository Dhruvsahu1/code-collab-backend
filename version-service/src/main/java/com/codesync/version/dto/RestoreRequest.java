package com.codesync.version.dto;

public class RestoreRequest {
    private Long snapshotId;
    private Long authorId;
    private String message;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}