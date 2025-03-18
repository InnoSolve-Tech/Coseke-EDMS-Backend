package com.edms.file_management.comment;

import com.edms.file_management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

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
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByDocumentId(Long documentId) {
        return commentRepository.findByDocumentId(documentId);
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
