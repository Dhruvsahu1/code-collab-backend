package com.codesync.comment.controller;

import com.codesync.comment.dto.CommentResponse;
import com.codesync.comment.dto.CreateCommentRequest;
import com.codesync.comment.entity.Comment;
import com.codesync.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@RequestBody CreateCommentRequest request) {
        Comment comment = commentService.addComment(request);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<CommentResponse>> getByFile(@PathVariable Long fileId) {
        List<Comment> comments = commentService.getByFile(fileId);
        List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CommentResponse>> getByProject(@PathVariable Long projectId) {
        List<Comment> comments = commentService.getByProject(projectId);
        List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }

    @GetMapping("/replies/{commentId}")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long commentId) {
        List<Comment> replies = commentService.getReplies(commentId);
        List<CommentResponse> responses = replies.stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String content = request.get("content");
        Comment comment = commentService.updateComment(id, content);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/resolve/{id}")
    public ResponseEntity<CommentResponse> resolveComment(@PathVariable Long id) {
        Comment comment = commentService.resolveComment(id);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }

    @PutMapping("/unresolve/{id}")
    public ResponseEntity<CommentResponse> unresolveComment(@PathVariable Long id) {
        Comment comment = commentService.unresolveComment(id);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }

    @GetMapping("/line")
    public ResponseEntity<List<CommentResponse>> getByLine(
            @RequestParam Long fileId,
            @RequestParam Integer line) {
        List<Comment> comments = commentService.getByLine(fileId, line);
        List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/count/{fileId}")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long fileId) {
        long count = commentService.getCommentCount(fileId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}