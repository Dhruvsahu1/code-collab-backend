package com.codesync.version.dto;

public class BranchRequest {
    private String branchName;
    private Long fromSnapshotId;

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public Long getFromSnapshotId() { return fromSnapshotId; }
    public void setFromSnapshotId(Long fromSnapshotId) { this.fromSnapshotId = fromSnapshotId; }
}