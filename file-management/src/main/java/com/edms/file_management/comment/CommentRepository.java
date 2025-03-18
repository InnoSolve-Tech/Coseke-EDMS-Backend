package com.edms.file_management.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
}

