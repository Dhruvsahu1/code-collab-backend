package com.codesync.comment.controller;

import com.codesync.comment.dto.CommentRequest;
import com.codesync.comment.dto.CommentResponse;
import com.codesync.comment.service.CommentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        logger.info("API HIT: POST /api/comments - projectId: {}, fileId: {}, authorId: {}, content: {}", 
            request.getProjectId(), request.getFileId(), request.getAuthorId(), request.getContent());
        try {
            logger.info("Attempting to save comment...");
            CommentResponse saved = commentService.addComment(request);
            logger.info("Comment saved successfully with id: {}", saved.getCommentId());
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            CommentResponse errorResponse = new CommentResponse();
            errorResponse.setCommentId(0L);
            errorResponse.setContent("Validation Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Error saving comment: ", e);
            CommentResponse errorResponse = new CommentResponse();
            errorResponse.setCommentId(0L);
            errorResponse.setContent("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping({"/file/{fileId}", "/file/{fileId}/comments"})
    public ResponseEntity<List<CommentResponse>> getFileComments(@PathVariable Long fileId) {
        logger.info("API HIT: GET /api/comments/file/{}", fileId);
        return ResponseEntity.ok(commentService.getByFile(fileId));
    }

    @GetMapping({"/project/{projectId}", "/project/{projectId}/comments"})
    public ResponseEntity<List<CommentResponse>> getProjectComments(@PathVariable Long projectId) {
        logger.info("API HIT: GET /api/comments/project/{}", projectId);
        return ResponseEntity.ok(commentService.getByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long id) {
        logger.info("API HIT: GET /comments/{}", id);
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @GetMapping({"/replies/{parentId}", "/replies/{parentId}/comments"})
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long parentId) {
        logger.info("API HIT: GET /comments/replies/{}", parentId);
        return ResponseEntity.ok(commentService.getReplies(parentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        logger.info("API HIT: PUT /comments/{}", id);
        String content = (String) payload.get("content");
        Long authorId = payload.get("authorId") != null ? ((Number) payload.get("authorId")).longValue() : 1L;
        return ResponseEntity.ok(commentService.updateComment(id, content, authorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        logger.info("API HIT: DELETE /comments/{}", id);
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/resolve/{id}", "/resolve/{id}/comments"})
    public ResponseEntity<CommentResponse> resolveComment(@PathVariable Long id) {
        logger.info("API HIT: POST /comments/resolve/{}", id);
        return ResponseEntity.ok(commentService.resolveComment(id));
    }

    @PutMapping({"/resolve/{id}", "/resolve/{id}/comments"})
    public ResponseEntity<CommentResponse> resolveCommentPut(@PathVariable Long id) {
        logger.info("API HIT: PUT /comments/resolve/{}", id);
        return ResponseEntity.ok(commentService.resolveComment(id));
    }

    @PutMapping({"/unresolve/{id}", "/unresolve/{id}/comments"})
    public ResponseEntity<CommentResponse> unresolveComment(@PathVariable Long id) {
        logger.info("API HIT: PUT /comments/unresolve/{}", id);
        return ResponseEntity.ok(commentService.unresolveComment(id));
    }

    @GetMapping({"/line", "/line/comments"})
    public ResponseEntity<List<CommentResponse>> getByLine(
            @RequestParam Long fileId,
            @RequestParam Integer line) {
        logger.info("API HIT: GET /comments/line?fileId={}&line={}", fileId, line);
        return ResponseEntity.ok(commentService.getByLine(fileId, line));
    }

    @GetMapping({"/count/{fileId}", "/count/{fileId}/comments"})
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long fileId) {
        logger.info("API HIT: GET /comments/count/{}", fileId);
        return ResponseEntity.ok(Map.of("count", commentService.getCommentCount(fileId)));
    }

    @GetMapping({"/unresolved/{fileId}", "/unresolved/{fileId}/comments"})
    public ResponseEntity<List<CommentResponse>> getUnresolvedComments(@PathVariable Long fileId) {
        logger.info("API HIT: GET /comments/unresolved/{}", fileId);
        return ResponseEntity.ok(commentService.getUnresolvedByFile(fileId));
    }
}