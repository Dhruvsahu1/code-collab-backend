package com.codesync.version.dto;

public class TagRequest {
    private Long snapshotId;
    private String tag;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}