package com.codesync.comment.service;

import com.codesync.comment.dto.CreateCommentRequest;
import com.codesync.comment.entity.Comment;
import com.codesync.comment.feign.FileClient;
import com.codesync.comment.feign.NotificationClient;
import com.codesync.comment.feign.ProjectClient;
import com.codesync.comment.feign.VersionClient;
import com.codesync.comment.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private final CommentRepository commentRepository;
    private final ProjectClient projectClient;
    private final FileClient fileClient;
    private final VersionClient versionClient;
    private final NotificationClient notificationClient;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            ProjectClient projectClient,
            FileClient fileClient,
            VersionClient versionClient,
            NotificationClient notificationClient) {
        this.commentRepository = commentRepository;
        this.projectClient = projectClient;
        this.fileClient = fileClient;
        this.versionClient = versionClient;
        this.notificationClient = notificationClient;
    }

    @Override
    @Transactional
    public Comment addComment(CreateCommentRequest request) {
        ResponseEntity<Object> projectResponse = projectClient.getProject(request.getProjectId());
        if (!projectResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Invalid project ID");
        }

        ResponseEntity<Object> fileResponse = fileClient.getFile(request.getFileId());
        if (!fileResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Invalid file ID");
        }

        if (request.getSnapshotId() != null) {
            ResponseEntity<Object> snapshotResponse = versionClient.getSnapshot(request.getSnapshotId());
            if (!snapshotResponse.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Invalid snapshot ID");
            }
        }

        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (!parentComment.getFileId().equals(request.getFileId())) {
                throw new IllegalArgumentException("Parent comment belongs to different file");
            }
        }

        Comment comment = Comment.builder()
                .projectId(request.getProjectId())
                .fileId(request.getFileId())
                .authorId(request.getAuthorId())
                .content(request.getContent())
                .lineNumber(request.getLineNumber())
                .columnNumber(request.getColumnNumber())
                .parentCommentId(request.getParentCommentId())
                .snapshotId(request.getSnapshotId())
                .resolved(false)
                .build();

        Comment savedComment = commentRepository.save(comment);

        processMentions(request.getContent(), savedComment);

        return savedComment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getByFile(Long fileId) {
        return commentRepository.findByFileId(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getByProject(Long projectId) {
        return commentRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getReplies(Long commentId) {
        return commentRepository.findByParentCommentId(commentId);
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, String content) {
        Comment comment = getCommentById(commentId);
        comment.setContent(content);
        Comment updated = commentRepository.save(comment);

        processMentions(content, updated);

        return updated;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public Comment resolveComment(Long commentId) {
        Comment comment = getCommentById(commentId);
        comment.setResolved(true);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment unresolveComment(Long commentId) {
        Comment comment = getCommentById(commentId);
        comment.setResolved(false);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getByLine(Long fileId, Integer lineNumber) {
        return commentRepository.findByFileIdAndLineNumber(fileId, lineNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(Long fileId) {
        return commentRepository.countByFileId(fileId);
    }

    private void processMentions(String content, Comment comment) {
        if (content == null || content.isEmpty()) {
            return;
        }

        Matcher matcher = MENTION_PATTERN.matcher(content);
        List<String> mentionedUsers = new ArrayList<>();

        while (matcher.find()) {
            mentionedUsers.add(matcher.group(1));
        }

        for (String username : mentionedUsers) {
            try {
                Map<String, Object> notification = Map.of(
                        "type", "MENTION",
                        "username", username,
                        "commentId", comment.getCommentId(),
                        "fileId", comment.getFileId(),
                        "projectId", comment.getProjectId(),
                        "content", content.substring(0, Math.min(100, content.length()))
                );
                notificationClient.sendMentionNotification(notification);
            } catch (Exception e) {
                log.warn("Failed to send mention notification to user: {}", username, e);
            }
        }
    }
}