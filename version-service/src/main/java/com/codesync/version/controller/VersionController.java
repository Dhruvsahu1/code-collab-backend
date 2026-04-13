package com.codesync.version.controller;

import com.codesync.version.dto.CreateSnapshotRequest;
import com.codesync.version.dto.SnapshotResponse;
import com.codesync.version.entity.Snapshot;
import com.codesync.version.service.VersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/versions")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @PostMapping
    public ResponseEntity<SnapshotResponse> createSnapshot(@RequestBody CreateSnapshotRequest request) {
        Snapshot snapshot = versionService.createSnapshot(request);
        return ResponseEntity.ok(SnapshotResponse.fromEntity(snapshot));
    }

    @GetMapping("/{snapshotId}")
    public ResponseEntity<SnapshotResponse> getSnapshot(@PathVariable Long snapshotId) {
        Snapshot snapshot = versionService.getSnapshotById(snapshotId);
        return ResponseEntity.ok(SnapshotResponse.fromEntity(snapshot));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<SnapshotResponse>> getSnapshotsByFile(@PathVariable Long fileId) {
        List<Snapshot> snapshots = versionService.getSnapshotsByFile(fileId);
        List<SnapshotResponse> responses = snapshots.stream()
                .map(SnapshotResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SnapshotResponse>> getSnapshotsByProject(@PathVariable Long projectId) {
        List<Snapshot> snapshots = versionService.getSnapshotsByProject(projectId);
        List<SnapshotResponse> responses = snapshots.stream()
                .map(SnapshotResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/branch/{branch}")
    public ResponseEntity<List<SnapshotResponse>> getSnapshotsByBranch(@PathVariable String branch) {
        List<Snapshot> snapshots = versionService.getSnapshotsByBranch(branch);
        List<SnapshotResponse> responses = snapshots.stream()
                .map(SnapshotResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/latest/{fileId}")
    public ResponseEntity<SnapshotResponse> getLatestSnapshot(@PathVariable Long fileId) {
        Snapshot snapshot = versionService.getLatestSnapshot(fileId);
        return ResponseEntity.ok(SnapshotResponse.fromEntity(snapshot));
    }

    @PostMapping("/restore/{snapshotId}")
    public ResponseEntity<SnapshotResponse> restoreSnapshot(@PathVariable Long snapshotId) {
        Snapshot snapshot = versionService.restoreSnapshot(snapshotId);
        return ResponseEntity.ok(SnapshotResponse.fromEntity(snapshot));
    }

    @GetMapping("/diff")
    public ResponseEntity<String> diffSnapshots(
            @RequestParam Long snap1,
            @RequestParam Long snap2) {
        String diff = versionService.diffSnapshots(snap1, snap2);
        return ResponseEntity.ok(diff);
    }

    @PostMapping("/branch")
    public ResponseEntity<Void> createBranch(@RequestBody Map<String, Object> request) {
        Long snapshotId = Long.parseLong(request.get("snapshotId").toString());
        String branchName = request.get("branchName").toString();
        versionService.createBranch(snapshotId, branchName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tag")
    public ResponseEntity<Void> tagSnapshot(@RequestBody Map<String, Object> request) {
        Long snapshotId = Long.parseLong(request.get("snapshotId").toString());
        String tag = request.get("tag").toString();
        versionService.tagSnapshot(snapshotId, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{fileId}")
    public ResponseEntity<List<SnapshotResponse>> getFileHistory(@PathVariable Long fileId) {
        List<Snapshot> snapshots = versionService.getFileHistory(fileId);
        List<SnapshotResponse> responses = snapshots.stream()
                .map(SnapshotResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }
}