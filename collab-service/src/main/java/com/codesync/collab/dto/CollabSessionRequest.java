package com.codesync.collab.dto;

public class CollabSessionRequest {
    private Long fileId;
    private Long ownerId;

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}