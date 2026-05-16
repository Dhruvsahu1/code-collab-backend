package com.codesync.version.controller;

import com.codesync.version.dto.BranchRequest;
import com.codesync.version.dto.CreateSnapshotRequest;
import com.codesync.version.dto.DiffResponse;
import com.codesync.version.dto.RestoreRequest;
import com.codesync.version.dto.SnapshotResponse;
import com.codesync.version.dto.TagRequest;
import com.codesync.version.entity.Snapshot;
import com.codesync.version.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/versions")
public class VersionController {

    private static final Logger logger = LoggerFactory.getLogger(VersionController.class);
    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    // ── Snapshot lifecycle ──────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Snapshot> createSnapshot(@RequestBody CreateSnapshotRequest request) {
        logger.info("API HIT: POST /versions - Creating snapshot for fileId: {}, branch: {}",
            request.getFileId(), request.getBranch());
        
        if (request.getAuthorId() == null) {
            throw new IllegalArgumentException("Author ID is required");
        }
        
        if (request.getProjectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }
        
        Snapshot snapshot = versionService.createSnapshot(request);
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/{snapshotId}")
    public ResponseEntity<Snapshot> getSnapshot(@PathVariable Long snapshotId) {
        logger.info("API HIT: GET /versions/{}", snapshotId);
        return ResponseEntity.ok(versionService.getById(snapshotId));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<Snapshot>> getSnapshotsByFile(@PathVariable Long fileId) {
        logger.info("API HIT: GET /versions/file/{}", fileId);
        return ResponseEntity.ok(versionService.getByFile(fileId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Snapshot>> getSnapshotsByProject(@PathVariable Long projectId) {
        logger.info("API HIT: GET /versions/project/{}", projectId);
        return ResponseEntity.ok(versionService.getByProject(projectId));
    }

    @GetMapping("/file/{fileId}/history")
    public ResponseEntity<List<SnapshotResponse>> getFileHistory(@PathVariable Long fileId) {
        logger.info("API HIT: GET /versions/file/{}/history", fileId);
        List<SnapshotResponse> history = versionService.getFileHistoryWithAuthorInfo(fileId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/project/{projectId}/history")
    public ResponseEntity<List<SnapshotResponse>> getProjectHistory(@PathVariable Long projectId) {
        logger.info("API HIT: GET /versions/project/{}/history", projectId);
        List<SnapshotResponse> history = versionService.getProjectHistoryWithAuthorInfo(projectId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/project/{projectId}/branch/{branch}/history")
    public ResponseEntity<List<SnapshotResponse>> getBranchHistory(
        @PathVariable Long projectId,
        @PathVariable String branch
    ) {
        logger.info("API HIT: GET /versions/project/{}/branch/{}/history", projectId, branch);
        List<SnapshotResponse> history = versionService.getBranchHistoryWithAuthorInfo(projectId, branch);
        return ResponseEntity.ok(history);
    }

    // ── Latest snapshot ─────────────────────────────────────────────────────────

    @GetMapping("/file/{fileId}/latest")
    public ResponseEntity<Snapshot> getLatestSnapshotForFile(
        @PathVariable Long fileId,
        @RequestParam(required = false, defaultValue = "main") String branch
    ) {
        logger.info("API HIT: GET /versions/file/{}/latest?branch={}", fileId, branch);
        return ResponseEntity.ok(versionService.getLatestSnapshot(fileId, branch));
    }

    // ── Restore ─────────────────────────────────────────────────────────────────

    @PostMapping("/restore")
    public ResponseEntity<Snapshot> restoreSnapshot(@RequestBody RestoreRequest request) {
        logger.info("API HIT: POST /versions/restore - Restoring snapshot ID: {}", request.getSnapshotId());
        Snapshot restored = versionService.restoreSnapshot(request);
        return ResponseEntity.ok(restored);
    }

    // ── Diff ─────────────────────────────────────────────────────────────────────

    @GetMapping("/diff/file/{fileId}")
    public ResponseEntity<DiffResponse> diffSnapshots(
        @PathVariable Long fileId,
        @RequestParam Long snapshot1Id,
        @RequestParam Long snapshot2Id
    ) {
        logger.info("API HIT: GET /versions/diff/file/{}?snapshot1Id={}&snapshot2Id={}",
            fileId, snapshot1Id, snapshot2Id);
        DiffResponse diff = versionService.compare(fileId, snapshot1Id, snapshot2Id);
        return ResponseEntity.ok(diff);
    }

    @GetMapping("/diff/snapshots/{snapshot1Id}/{snapshot2Id}")
    public ResponseEntity<DiffResponse> diffBetweenSnapshots(
        @PathVariable Long snapshot1Id,
        @PathVariable Long snapshot2Id
    ) {
        logger.info("API HIT: GET /versions/diff/snapshots/{}/{}", snapshot1Id, snapshot2Id);
        DiffResponse diff = versionService.compareSnapshots(snapshot1Id, snapshot2Id);
        return ResponseEntity.ok(diff);
    }

    // ── Branching ───────────────────────────────────────────────────────────────

    @GetMapping("/branches/project/{projectId}")
    public ResponseEntity<List<String>> getBranches(@PathVariable Long projectId) {
        logger.info("API HIT: GET /versions/branches/project/{}", projectId);
        List<String> branches = versionService.getBranches(projectId);
        return ResponseEntity.ok(branches);
    }

    @PostMapping("/branch")
    public ResponseEntity<Snapshot> createBranch(@RequestBody BranchRequest request) {
        logger.info("API HIT: POST /versions/branch - Creating branch: {}", request.getBranchName());
        Snapshot branchSnapshot = versionService.createBranch(request);
        return ResponseEntity.ok(branchSnapshot);
    }

    @GetMapping("/branch/exists")
    public ResponseEntity<Boolean> branchExists(
        @RequestParam Long projectId,
        @RequestParam String branch
    ) {
        boolean exists = versionService.branchExists(projectId, branch);
        return ResponseEntity.ok(exists);
    }

    // ── Tagging ──────────────────────────────────────────────────────────────────

    @PostMapping("/tag")
    public ResponseEntity<Snapshot> tagSnapshot(@RequestBody TagRequest request) {
        logger.info("API HIT: POST /versions/tag - Tagging snapshot ID: {} with tag: {}",
            request.getSnapshotId(), request.getTag());
        Snapshot tagged = versionService.tagSnapshot(request.getSnapshotId(), request.getTag());
        return ResponseEntity.ok(tagged);
    }

    @GetMapping("/tag/project/{projectId}/file/{fileId}")
    public ResponseEntity<Snapshot> getSnapshotByTag(
        @PathVariable Long projectId,
        @PathVariable Long fileId,
        @RequestParam String tag
    ) {
        logger.info("API HIT: GET /versions/tag/project/{}/file/{}?tag={}", projectId, fileId, tag);
        return ResponseEntity.ok(versionService.getSnapshotByTag(projectId, fileId, tag));
    }

    // ── Utils ────────────────────────────────────────────────────────────────────

    @GetMapping("/snapshot-count/file/{fileId}")
    public ResponseEntity<Integer> getSnapshotCountByFile(@PathVariable Long fileId) {
        long count = versionService.getByFile(fileId).size();
        return ResponseEntity.ok((int) count);
    }

    @GetMapping("/snapshot-count/project/{projectId}")
    public ResponseEntity<Integer> getSnapshotCountByProject(@PathVariable Long projectId) {
        long count = versionService.getByProject(projectId).size();
        return ResponseEntity.ok((int) count);
    }
}