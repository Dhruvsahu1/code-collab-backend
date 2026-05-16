package com.codesync.comment.service;

import com.codesync.comment.client.NotificationClient;
import com.codesync.comment.client.VersionClient;
import com.codesync.comment.dto.CommentRequest;
import com.codesync.comment.dto.CommentResponse;
import com.codesync.comment.entity.Comment;
import com.codesync.comment.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final VersionClient versionClient;
    private final NotificationClient notificationClient;

    public CommentServiceImpl(CommentRepository commentRepository, VersionClient versionClient, NotificationClient notificationClient) {
        this.commentRepository = commentRepository;
        this.versionClient = versionClient;
        this.notificationClient = notificationClient;
    }

    @Override
    @Transactional
    public CommentResponse addComment(CommentRequest request) {
        logger.info("Adding comment - projectId: {}, fileId: {}, authorId: {}, content: {}", 
            request.getProjectId(), request.getFileId(), request.getAuthorId(), request.getContent());

        if (request.getProjectId() == null || request.getFileId() == null) {
            throw new IllegalArgumentException("Project ID and File ID are required");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content is required");
        }

        Long snapshotId = request.getSnapshotId();
        if (snapshotId == null) {
            try {
                snapshotId = getLatestSnapshotId(request.getFileId());
                logger.info("Using latest snapshot: {}", snapshotId);
            } catch (Exception e) {
                logger.warn("Could not get latest snapshot, continuing without snapshot: {}", e.getMessage());
            }
        }

        Comment comment = new Comment();
        comment.setProjectId(request.getProjectId());
        comment.setFileId(request.getFileId());
        comment.setAuthorId(request.getAuthorId());
        comment.setContent(request.getContent());
        comment.setLineNumber(request.getLineNumber() != null ? request.getLineNumber() : 1);
        comment.setColumnNumber(request.getColumnNumber());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setSnapshotId(snapshotId);
        comment.setResolved(false);

        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            
            boolean parentIsReply = parent.getParentCommentId() != null;
            if (parentIsReply) {
                throw new IllegalArgumentException("Cannot nest replies more than 2 levels");
            }
        }

        Comment saved = commentRepository.save(comment);
        logger.info("Comment saved successfully with id: {}", saved.getCommentId());

        sendCommentNotifications(saved);

        return CommentResponse.fromEntity(saved);
    }

    private Long getLatestSnapshotId(Long fileId) {
        try {
            Map<String, Object> snapshot = versionClient.getLatestSnapshot(fileId);
            if (snapshot != null && snapshot.get("snapshotId") != null) {
                return ((Number) snapshot.get("snapshotId")).longValue();
            }
        } catch (Exception e) {
            logger.warn("Could not get latest snapshot: {}", e.getMessage());
        }
        return null;
    }

    private void sendCommentNotifications(Comment comment) {
        try {
            if (comment.getParentCommentId() != null) {
                sendReplyNotification(comment);
            } else {
                sendNewCommentNotification(comment);
            }
        } catch (Exception e) {
            logger.warn("Failed to send comment notifications: {}", e.getMessage());
        }
    }

    private void sendNewCommentNotification(Comment comment) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("recipientId", comment.getProjectId());
            notification.put("actorId", comment.getAuthorId());
            notification.put("type", "COMMENT");
            notification.put("title", "New Comment");
            notification.put("message", "A new comment was added to your file");
            notification.put("relatedId", comment.getFileId());
            notification.put("relatedType", "FILE");
            notification.put("sendEmail", false);

            notificationClient.createNotification(notification);
            logger.info("Sent new comment notification for file: {}", comment.getFileId());
        } catch (Exception e) {
            logger.warn("Failed to send new comment notification: {}", e.getMessage());
        }
    }

    private void sendReplyNotification(Comment comment) {
        try {
            Comment parentComment = commentRepository.findById(comment.getParentCommentId()).orElse(null);
            if (parentComment != null && !parentComment.getAuthorId().equals(comment.getAuthorId())) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("recipientId", parentComment.getAuthorId());
                notification.put("actorId", comment.getAuthorId());
                notification.put("type", "COMMENT");
                notification.put("title", "New Reply");
                notification.put("message", "Someone replied to your comment");
                notification.put("relatedId", comment.getCommentId());
                notification.put("relatedType", "COMMENT");
                notification.put("sendEmail", false);

                notificationClient.createNotification(notification);
                logger.info("Sent reply notification to user: {}", parentComment.getAuthorId());
            }
        } catch (Exception e) {
            logger.warn("Failed to send reply notification: {}", e.getMessage());
        }
    }

    @Override
    public List<CommentResponse> getByFile(Long fileId) {
        List<Comment> comments = commentRepository.findByFileId(fileId);
        comments.sort(Comparator.comparing(Comment::getCreatedAt));

        List<CommentResponse> responses = new ArrayList<>();
        Map<Long, CommentResponse> responseMap = new HashMap<>();

        for (Comment comment : comments) {
            CommentResponse response = CommentResponse.fromEntity(comment);
            responseMap.put(comment.getCommentId(), response);
            
            if (comment.getParentCommentId() == null) {
                responses.add(response);
            }
        }

        for (Comment comment : comments) {
            if (comment.getParentCommentId() != null) {
                CommentResponse parent = responseMap.get(comment.getParentCommentId());
                if (parent != null) {
                    parent.getReplies().add(CommentResponse.fromEntity(comment));
                }
            }
        }

        return responses;
    }

    @Override
    public List<CommentResponse> getByProject(Long projectId) {
        List<Comment> comments = commentRepository.findByProjectId(projectId);
        return comments.stream()
            .map(CommentResponse::fromEntity)
            .toList();
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
            .map(CommentResponse::fromEntity)
            .orElse(null);
    }

    @Override
    public List<CommentResponse> getReplies(Long parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId)
            .stream()
            .map(CommentResponse::fromEntity)
            .toList();
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, String content, Long authorId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("Only the author can update this comment");
        }

        comment.setContent(content);
        Comment saved = commentRepository.save(comment);
        logger.info("Comment {} updated", commentId);

        return CommentResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        List<Comment> replies = commentRepository.findByParentCommentId(commentId);
        for (Comment reply : replies) {
            commentRepository.deleteById(reply.getCommentId());
        }
        commentRepository.deleteById(commentId);
        logger.info("Comment {} deleted with {} replies", commentId, replies.size());
    }

    @Override
    @Transactional
    public CommentResponse resolveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        comment.setResolved(true);
        Comment saved = commentRepository.save(comment);
        logger.info("Comment {} resolved", commentId);

        return CommentResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CommentResponse unresolveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        comment.setResolved(false);
        Comment saved = commentRepository.save(comment);
        logger.info("Comment {} unresolved", commentId);

        return CommentResponse.fromEntity(saved);
    }

    @Override
    public List<CommentResponse> getByLine(Long fileId, Integer lineNumber) {
        return commentRepository.findByFileIdAndLineNumber(fileId, lineNumber)
            .stream()
            .map(CommentResponse::fromEntity)
            .toList();
    }

    @Override
    public Long getCommentCount(Long fileId) {
        return commentRepository.countByFileId(fileId);
    }

    @Override
    public List<CommentResponse> getUnresolvedByFile(Long fileId) {
        return commentRepository.findByFileIdAndResolved(fileId, false)
            .stream()
            .map(CommentResponse::fromEntity)
            .toList();
    }
}
