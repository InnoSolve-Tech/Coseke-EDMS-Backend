package com.edms.file_management.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);

    @Query(value = "SELECT email, first_name AS firstName, last_name AS lastName, phone FROM users WHERE id = :userId", nativeQuery = true)
    Map<String, Object> getUserDetailsById(@Param("userId") Long userId);

}