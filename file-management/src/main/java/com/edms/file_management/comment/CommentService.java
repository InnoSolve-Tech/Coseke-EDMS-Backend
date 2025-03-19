package com.edms.file_management.comment;

import com.edms.file_management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment addComment(Long documentId, Long userId, String content) {
        // Fetch user details before creating the comment
        Map<String, Object> userDetails = commentRepository.getUserDetailsById(userId);

        if (userDetails == null || userDetails.isEmpty()) {
            throw new RuntimeException("User details not found for user ID: " + userId);
        }

        // Create comment with user details
        Comment comment = Comment.builder()
                .documentId(documentId)
                .userId(userId)
                .content(content)
                .userEmail((String) userDetails.getOrDefault("email", "N/A"))
                .userFirstName((String) userDetails.getOrDefault("firstName", "Unknown"))
                .userLastName((String) userDetails.getOrDefault("lastName", "Unknown"))
                .userPhone((String) userDetails.getOrDefault("phone", "Not Provided"))
                .createdAt(new Date())  // Ensure timestamps are set
                .updatedAt(new Date())
                .build();

        // Save comment and flush to ensure data is written to the database
        return commentRepository.saveAndFlush(comment);
    }


    public List<Comment> getCommentsByDocumentId(Long documentId) {
        List<Comment> comments = commentRepository.findByDocumentId(documentId);

        // Fetch user details for each comment
        for (Comment comment : comments) {
            Map<String, Object> userDetails = commentRepository.getUserDetailsById(comment.getUserId());
            comment.setUserEmail((String) userDetails.get("email"));
            comment.setUserFirstName((String) userDetails.get("firstName"));
            comment.setUserLastName((String) userDetails.get("lastName"));
            comment.setUserPhone((String) userDetails.get("phone"));
        }

        return comments;
    }

    @Transactional
    public Comment updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with ID: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }@Transactional
    public Comment updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        // ✅ Ownership Check
        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("User not authorized to edit this comment.");
        }

        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("User not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
    }
}
