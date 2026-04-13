package com.codesync.version.service;

import com.codesync.version.dto.CreateSnapshotRequest;
import com.codesync.version.entity.Snapshot;
import com.codesync.version.feign.FileClient;
import com.codesync.version.feign.ProjectClient;
import com.codesync.version.repository.SnapshotRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VersionServiceImpl implements VersionService {

    private final SnapshotRepository snapshotRepository;
    private final ProjectClient projectClient;
    private final FileClient fileClient;

    public VersionServiceImpl(
            SnapshotRepository snapshotRepository,
            ProjectClient projectClient,
            FileClient fileClient) {
        this.snapshotRepository = snapshotRepository;
        this.projectClient = projectClient;
        this.fileClient = fileClient;
    }

    @Override
    @Transactional
    public Snapshot createSnapshot(CreateSnapshotRequest request) {
        ResponseEntity<Object> projectResponse = projectClient.getProject(request.getProjectId());
        if (!projectResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Invalid project ID");
        }

        ResponseEntity<Object> fileResponse = fileClient.getFile(request.getFileId());
        if (!fileResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Invalid file ID");
        }

        Optional<Snapshot> latestSnapshot = snapshotRepository.findTopByFileIdOrderByCreatedAtDesc(request.getFileId());

        Snapshot snapshot = Snapshot.builder()
                .projectId(request.getProjectId())
                .fileId(request.getFileId())
                .authorId(request.getAuthorId())
                .message(request.getMessage())
                .content(request.getContent())
                .hash(computeHash(request.getContent()))
                .parentSnapshotId(latestSnapshot.map(Snapshot::getSnapshotId).orElse(null))
                .branch(request.getBranch() != null ? request.getBranch() : "main")
                .build();

        return snapshotRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Snapshot getSnapshotById(Long snapshotId) {
        return snapshotRepository.findBySnapshotId(snapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + snapshotId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> getSnapshotsByFile(Long fileId) {
        return snapshotRepository.findByFileIdOrderByCreatedAtDesc(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> getSnapshotsByProject(Long projectId) {
        return snapshotRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> getSnapshotsByBranch(String branch) {
        return snapshotRepository.findByBranch(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public Snapshot getLatestSnapshot(Long fileId) {
        return snapshotRepository.findTopByFileIdOrderByCreatedAtDesc(fileId)
                .orElseThrow(() -> new IllegalArgumentException("No snapshots found for file: " + fileId));
    }

    @Override
    @Transactional
    public Snapshot restoreSnapshot(Long snapshotId) {
        Snapshot snapshot = getSnapshotById(snapshotId);

        CreateSnapshotRequest restoreRequest = CreateSnapshotRequest.builder()
                .projectId(snapshot.getProjectId())
                .fileId(snapshot.getFileId())
                .authorId(snapshot.getAuthorId())
                .message("Restored from snapshot: " + snapshotId)
                .content(snapshot.getContent())
                .branch(snapshot.getBranch())
                .build();

        return createSnapshot(restoreRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public String diffSnapshots(Long snap1, Long snap2) {
        Snapshot s1 = getSnapshotById(snap1);
        Snapshot s2 = getSnapshotById(snap2);

        String[] lines1 = s1.getContent().split("\n");
        String[] lines2 = s2.getContent().split("\n");

        StringBuilder diff = new StringBuilder();
        diff.append("--- Snapshot ").append(snap1).append("\n");
        diff.append("+++ Snapshot ").append(snap2).append("\n");

        List<String> list1 = new ArrayList<>(List.of(lines1));
        List<String> list2 = new ArrayList<>(List.of(lines2));

        int maxLines = Math.max(list1.size(), list2.size());
        for (int i = 0; i < maxLines; i++) {
            String line1 = i < list1.size() ? list1.get(i) : "";
            String line2 = i < list2.size() ? list2.get(i) : "";

            if (!line1.equals(line2)) {
                if (line1.isEmpty()) {
                    diff.append("+").append(i + 1).append(": ").append(line2).append("\n");
                } else if (line2.isEmpty()) {
                    diff.append("-").append(i + 1).append(": ").append(line1).append("\n");
                } else {
                    diff.append("-").append(i + 1).append(": ").append(line1).append("\n");
                    diff.append("+").append(i + 1).append(": ").append(line2).append("\n");
                }
            }
        }

        return diff.toString();
    }

    @Override
    @Transactional
    public void createBranch(Long snapshotId, String branchName) {
        Snapshot snapshot = getSnapshotById(snapshotId);
        snapshot.setBranch(branchName);
        snapshotRepository.save(snapshot);
    }

    @Override
    @Transactional
    public void tagSnapshot(Long snapshotId, String tag) {
        Snapshot snapshot = getSnapshotById(snapshotId);
        snapshot.setTag(tag);
        snapshotRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> getFileHistory(Long fileId) {
        return snapshotRepository.findByFileIdOrderByCreatedAtDesc(fileId);
    }

    private String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}