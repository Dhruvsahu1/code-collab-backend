package com.codesync.version.service;

import com.codesync.version.dto.CreateSnapshotRequest;
import com.codesync.version.entity.Snapshot;

import java.util.List;

public interface VersionService {

    Snapshot createSnapshot(CreateSnapshotRequest request);

    Snapshot getSnapshotById(Long snapshotId);

    List<Snapshot> getSnapshotsByFile(Long fileId);

    List<Snapshot> getSnapshotsByProject(Long projectId);

    List<Snapshot> getSnapshotsByBranch(String branch);

    Snapshot getLatestSnapshot(Long fileId);

    Snapshot restoreSnapshot(Long snapshotId);

    String diffSnapshots(Long snap1, Long snap2);

    void createBranch(Long snapshotId, String branchName);

    void tagSnapshot(Long snapshotId, String tag);

    List<Snapshot> getFileHistory(Long fileId);
}