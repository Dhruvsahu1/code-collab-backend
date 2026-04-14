package com.codesync.comment.repository;

import com.codesync.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByFileId(Long fileId);

    List<Comment> findByProjectId(Long projectId);

    List<Comment> findByAuthorId(Long authorId);

    List<Comment> findByLineNumber(Integer lineNumber);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByResolved(Boolean resolved);

    long countByFileId(Long fileId);

    List<Comment> findByFileIdAndLineNumber(Long fileId, Integer lineNumber);
}