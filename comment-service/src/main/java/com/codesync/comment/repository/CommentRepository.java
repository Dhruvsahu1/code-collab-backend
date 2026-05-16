package com.codesync.comment.repository;

import com.codesync.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByFileId(Long fileId);

    List<Comment> findByProjectId(Long projectId);

    List<Comment> findByAuthorId(Long authorId);

    List<Comment> findByLineNumberAndFileId(Integer lineNumber, Long fileId);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByParentCommentId(Long parentCommentId, Sort sort);

    List<Comment> findByResolved(Boolean resolved);

    List<Comment> findByResolvedAndFileId(Boolean resolved, Long fileId);

    long countByFileId(Long fileId);

    List<Comment> findByFileIdAndLineNumber(Long fileId, Integer lineNumber);

    Page<Comment> findByFileId(Long fileId, Pageable pageable);

    Page<Comment> findByProjectId(Long projectId, Pageable pageable);

    List<Comment> findTopLevelByFileId(Long fileId, Sort sort);

    List<Comment> findByFileIdAndResolved(Long fileId, Boolean resolved, Sort sort);

    boolean existsByParentCommentId(Long parentCommentId);

    void deleteByCommentId(Long commentId);
}