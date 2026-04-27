package com.codesync.version.service;

import com.codesync.version.dto.DiffResponse;
import com.codesync.version.dto.SnapshotResponse;
import com.codesync.version.entity.Snapshot;
import java.util.List;

public interface VersionService {

    Snapshot createSnapshot(com.codesync.version.dto.CreateSnapshotRequest request);

    List<Snapshot> getByFile(Long fileId);

    List<Snapshot> getByProject(Long projectId);

    Snapshot getById(Long id);

    Snapshot getByHash(String hash, Long fileId);

    List<Snapshot> getBranch(Long projectId, String branch);

    Snapshot tagSnapshot(Long id, String tag);

    DiffResponse compare(Long fileId, Long s1, Long s2);

    DiffResponse compareSnapshots(Long snapshot1Id, Long snapshot2Id);

    Snapshot restoreSnapshot(com.codesync.version.dto.RestoreRequest request);

    Snapshot createBranch(com.codesync.version.dto.BranchRequest request);

    List<String> getBranches(Long projectId);

    boolean branchExists(Long projectId, String branch);

    Snapshot getLatestSnapshot(Long fileId, String branch);

    Snapshot getSnapshotByTag(Long projectId, Long fileId, String tag);

    List<SnapshotResponse> getFileHistoryWithAuthorInfo(Long fileId);

    List<SnapshotResponse> getProjectHistoryWithAuthorInfo(Long projectId);

    List<SnapshotResponse> getBranchHistoryWithAuthorInfo(Long projectId, String branch);

    Snapshot getLatestSnapshotByFileAndBranch(Long fileId, String branch);
}
