package com.edms.file_management.comment;

import com.edms.file_management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
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
        Comment comment = Comment.builder()
                .documentId(documentId)
                .userId(userId)
                .content(content)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Fetch user details and set them in the comment
        Map<String, Object> userDetails = commentRepository.getUserDetailsById(userId);
        savedComment.setUserEmail((String) userDetails.get("email"));
        savedComment.setUserFirstName((String) userDetails.get("firstName"));
        savedComment.setUserLastName((String) userDetails.get("lastName"));
        savedComment.setUserPhone((String) userDetails.get("phone"));

        return savedComment;
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
