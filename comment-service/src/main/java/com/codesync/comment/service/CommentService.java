package com.codesync.comment.service;

import com.codesync.comment.dto.CommentRequest;
import com.codesync.comment.dto.CommentResponse;
import java.util.List;

public interface CommentService {

    CommentResponse addComment(CommentRequest request);

    List<CommentResponse> getByFile(Long fileId);

    List<CommentResponse> getByProject(Long projectId);

    CommentResponse getCommentById(Long commentId);

    List<CommentResponse> getReplies(Long commentId);

    CommentResponse updateComment(Long commentId, String content, Long authorId);

    void deleteComment(Long commentId);

    CommentResponse resolveComment(Long commentId);

    CommentResponse unresolveComment(Long commentId);

    List<CommentResponse> getByLine(Long fileId, Integer lineNumber);

    Long getCommentCount(Long fileId);

    List<CommentResponse> getUnresolvedByFile(Long fileId);
}