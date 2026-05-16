package com.codesync.version.service;

import com.codesync.version.client.NotificationClient;
import com.codesync.version.dto.BranchRequest;
import com.codesync.version.dto.CreateSnapshotRequest;
import com.codesync.version.dto.DiffResponse;
import com.codesync.version.dto.DiffLine;
import com.codesync.version.dto.RestoreRequest;
import com.codesync.version.dto.SnapshotResponse;
import com.codesync.version.dto.TagRequest;
import com.codesync.version.entity.Snapshot;
import com.codesync.version.repository.SnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VersionServiceImpl implements VersionService {

    private static final Logger logger = LoggerFactory.getLogger(VersionServiceImpl.class);
    private final SnapshotRepository snapshotRepository;
    private final NotificationClient notificationClient;

    public VersionServiceImpl(SnapshotRepository snapshotRepository, NotificationClient notificationClient) {
        this.snapshotRepository = snapshotRepository;
        this.notificationClient = notificationClient;
    }

    @Override
    public Snapshot createSnapshot(CreateSnapshotRequest request) {
        logger.info("Creating snapshot for fileId: {}, projectId: {}, branch: {}",
            request.getFileId(), request.getProjectId(), request.getBranch());

        String branch = request.getBranch() != null ? request.getBranch() : "main";
        String content = request.getContent();
        if (content == null) {
            throw new IllegalArgumentException("Snapshot content cannot be null");
        }

        // Generate SHA-256 hash
        String hash = generateHash(content);
        logger.debug("Generated hash: {}", hash);

        // Check for duplicate snapshot (same hash for same file)
        snapshotRepository.findByHashAndFileId(hash, request.getFileId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Identical snapshot already exists with ID: " + existing.getSnapshotId());
        });

        // Find latest snapshot on this branch for this file
        Optional<Snapshot> latestOpt = snapshotRepository
            .findFirstByFileIdAndBranchOrderByCreatedAtDesc(request.getFileId(), branch);

        Long parentSnapshotId = latestOpt.map(Snapshot::getSnapshotId).orElse(null);
        if (parentSnapshotId == null) {
            logger.info("First snapshot for fileId: {} on branch: {}", request.getFileId(), branch);
        }

        Snapshot snapshot = new Snapshot();
        snapshot.setProjectId(request.getProjectId());
        snapshot.setFileId(request.getFileId());
        snapshot.setAuthorId(request.getAuthorId());
        snapshot.setMessage(request.getMessage());
        snapshot.setContent(content);
        snapshot.setHash(hash);
        snapshot.setParentSnapshotId(parentSnapshotId);
        snapshot.setBranch(branch);
        snapshot.setTag(null);
        snapshot.setCreatedAt(LocalDateTime.now());

        Snapshot saved = snapshotRepository.save(snapshot);
        logger.info("Snapshot created: ID={}, branch={}", saved.getSnapshotId(), branch);

        sendSnapshotNotification(saved);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> getByFile(Long fileId) {
        return snapshotRepository.findByFileIdOrderByCreatedAtDesc(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> getByProject(Long projectId) {
        return snapshotRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public Snapshot getById(Long id) {
        return snapshotRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Snapshot getByHash(String hash, Long fileId) {
        return snapshotRepository.findByHashAndFileId(hash, fileId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Snapshot> getBranch(Long projectId, String branch) {
        return snapshotRepository.findByProjectIdAndBranchOrderByCreatedAtDesc(projectId, branch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getBranches(Long projectId) {
        return snapshotRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(Snapshot::getBranch)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean branchExists(Long projectId, String branch) {
        return !snapshotRepository.findByProjectIdAndBranchOrderByCreatedAtDesc(projectId, branch).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public Snapshot getLatestSnapshot(Long fileId, String branch) {
        return snapshotRepository
            .findFirstByFileIdAndBranchOrderByCreatedAtDesc(fileId, branch != null ? branch : "main")
            .orElse(null);
    }

    @Override
    public Snapshot tagSnapshot(Long snapshotId, String tag) {
        Snapshot snapshot = snapshotRepository.findById(snapshotId)
            .orElseThrow(() -> new NoSuchElementException("Snapshot not found: " + snapshotId));

        // Ensure tag uniqueness per file
        Optional<Snapshot> existing = snapshotRepository
            .findByProjectIdAndFileIdAndTag(snapshot.getProjectId(), snapshot.getFileId(), tag);
        if (existing.isPresent() && !existing.get().getSnapshotId().equals(snapshotId)) {
            throw new IllegalArgumentException("Tag '" + tag + "' already exists for this file");
        }

        snapshot.setTag(tag);
        return snapshotRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Snapshot getSnapshotByTag(Long projectId, Long fileId, String tag) {
        return snapshotRepository.findByProjectIdAndFileIdAndTag(projectId, fileId, tag)
            .orElse(null);
    }

    @Override
    public Snapshot restoreSnapshot(RestoreRequest request) {
        logger.info("Restoring snapshot ID: {} by authorId: {}", request.getSnapshotId(), request.getAuthorId());

        Snapshot target = snapshotRepository.findById(request.getSnapshotId())
            .orElseThrow(() -> new NoSuchElementException("Snapshot not found: " + request.getSnapshotId()));

        String branch = target.getBranch();
        Long parentId = snapshotRepository
            .findFirstByFileIdAndBranchOrderByCreatedAtDesc(target.getFileId(), branch)
            .map(Snapshot::getSnapshotId)
            .orElse(null);

        Snapshot restored = new Snapshot();
        restored.setProjectId(target.getProjectId());
        restored.setFileId(target.getFileId());
        restored.setAuthorId(request.getAuthorId());
        restored.setMessage(request.getMessage() != null ? request.getMessage() : "Restored snapshot " + request.getSnapshotId());
        restored.setContent(target.getContent());
        restored.setHash(target.getHash());
        restored.setParentSnapshotId(parentId);
        restored.setBranch(branch);
        restored.setTag(null);
        restored.setCreatedAt(LocalDateTime.now());

        Snapshot saved = snapshotRepository.save(restored);
        logger.info("Snapshot restored: new ID={} from original ID={}", saved.getSnapshotId(), request.getSnapshotId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public DiffResponse compare(Long fileId, Long snapshot1Id, Long snapshot2Id) {
        Snapshot s1 = snapshotRepository.findById(snapshot1Id)
            .orElseThrow(() -> new NoSuchElementException("Snapshot 1 not found: " + snapshot1Id));
        Snapshot s2 = snapshotRepository.findById(snapshot2Id)
            .orElseThrow(() -> new NoSuchElementException("Snapshot 2 not found: " + snapshot2Id));

        if (!s1.getFileId().equals(fileId) || !s2.getFileId().equals(fileId)) {
            throw new IllegalArgumentException("Snapshots must belong to the same file");
        }

        return computeDiff(s1, s2);
    }

    @Override
    @Transactional(readOnly = true)
    public DiffResponse compareSnapshots(Long snapshot1Id, Long snapshot2Id) {
        Snapshot s1 = snapshotRepository.findById(snapshot1Id)
            .orElseThrow(() -> new NoSuchElementException("Snapshot 1 not found: " + snapshot1Id));
        Snapshot s2 = snapshotRepository.findById(snapshot2Id)
            .orElseThrow(() -> new NoSuchElementException("Snapshot 2 not found: " + snapshot2Id));

        if (!s1.getFileId().equals(s2.getFileId())) {
            throw new IllegalArgumentException("Snapshots must belong to the same file");
        }

        return computeDiff(s1, s2);
    }

    private DiffResponse computeDiff(Snapshot older, Snapshot newer) {
        Snapshot s1, s2;
        if (older.getCreatedAt().isBefore(newer.getCreatedAt()) ||
            (older.getCreatedAt().isEqual(newer.getCreatedAt()) && older.getSnapshotId() < newer.getSnapshotId())) {
            s1 = older;
            s2 = newer;
        } else {
            s1 = newer;
            s2 = older;
        }

        List<String> lines1 = Arrays.asList(s1.getContent().split("\n"));
        List<String> lines2 = Arrays.asList(s2.getContent().split("\n"));

        int m = lines1.size();
        int n = lines2.size();

        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (lines1.get(i - 1).equals(lines2.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        LinkedList<DiffLine> reverse = new LinkedList<>();
        int i = m, j = n;
        int additions = 0, deletions = 0;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && lines1.get(i - 1).equals(lines2.get(j - 1))) {
                reverse.addFirst(new DiffLine(i, lines1.get(i - 1), "unchanged"));
                i--; j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                reverse.addFirst(new DiffLine(j, lines2.get(j - 1), "addition"));
                additions++; j--;
            } else {
                reverse.addFirst(new DiffLine(i, lines1.get(i - 1), "deletion"));
                deletions++; i--;
            }
        }

        DiffResponse resp = new DiffResponse();
        resp.setSnapshot1Id(s1.getSnapshotId());
        resp.setSnapshot2Id(s2.getSnapshotId());
        resp.setSnapshot1Hash(s1.getHash());
        resp.setSnapshot2Hash(s2.getHash());
        resp.setSnapshot1Time(s1.getCreatedAt());
        resp.setSnapshot2Time(s2.getCreatedAt());
        resp.setLines(reverse);
        resp.setAdditions(additions);
        resp.setDeletions(deletions);
        return resp;
    }

    private String generateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnapshotResponse> getFileHistoryWithAuthorInfo(Long fileId) {
        return snapshotRepository.findByFileIdOrderByCreatedAtDesc(fileId).stream()
            .map(this::enrichSnapshot)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnapshotResponse> getProjectHistoryWithAuthorInfo(Long projectId) {
        return snapshotRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(this::enrichSnapshot)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnapshotResponse> getBranchHistoryWithAuthorInfo(Long projectId, String branch) {
        return snapshotRepository.findByProjectIdAndBranchOrderByCreatedAtDesc(projectId, branch).stream()
            .map(this::enrichSnapshot)
            .collect(Collectors.toList());
    }

    private SnapshotResponse enrichSnapshot(Snapshot s) {
        SnapshotResponse r = SnapshotResponse.fromEntity(s);
        r.setAuthorName("User " + s.getAuthorId());
        return r;
    }

    @Override
    public Snapshot createBranch(BranchRequest req) {
        logger.info("Creating branch '{}' from snapshot ID: {}", req.getBranchName(), req.getFromSnapshotId());

        Snapshot src = snapshotRepository.findById(req.getFromSnapshotId())
            .orElseThrow(() -> new NoSuchElementException("Source snapshot not found: " + req.getFromSnapshotId()));

        if (branchExists(src.getProjectId(), req.getBranchName())) {
            throw new IllegalArgumentException("Branch '" + req.getBranchName() + "' already exists");
        }

        Snapshot branchSnap = new Snapshot();
        branchSnap.setProjectId(src.getProjectId());
        branchSnap.setFileId(src.getFileId());
        branchSnap.setAuthorId(src.getAuthorId());
        branchSnap.setMessage("Branch created: " + req.getBranchName());
        branchSnap.setContent(src.getContent());
        branchSnap.setHash(src.getHash());
        branchSnap.setParentSnapshotId(src.getParentSnapshotId());
        branchSnap.setBranch(req.getBranchName());
        branchSnap.setTag(null);
        branchSnap.setCreatedAt(LocalDateTime.now());

        Snapshot saved = snapshotRepository.save(branchSnap);
        logger.info("Branch '{}' created with snapshot ID: {}", req.getBranchName(), saved.getSnapshotId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Snapshot getLatestSnapshotByFileAndBranch(Long fileId, String branch) {
        return snapshotRepository
            .findFirstByFileIdAndBranchOrderByCreatedAtDesc(fileId, branch)
            .orElse(null);
    }

    private void sendSnapshotNotification(Snapshot snapshot) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("recipientId", snapshot.getProjectId());
            notification.put("actorId", snapshot.getAuthorId());
            notification.put("type", "SNAPSHOT");
            notification.put("title", "New Snapshot Created");
            notification.put("message", "A new version was saved for a file");
            notification.put("relatedId", snapshot.getSnapshotId());
            notification.put("relatedType", "SNAPSHOT");
            notification.put("sendEmail", false);
            notificationClient.createNotification(notification);
            logger.info("Sent snapshot notification for snapshot: {}", snapshot.getSnapshotId());
        } catch (Exception e) {
            logger.warn("Failed to send snapshot notification: {}", e.getMessage());
        }
    }
}
