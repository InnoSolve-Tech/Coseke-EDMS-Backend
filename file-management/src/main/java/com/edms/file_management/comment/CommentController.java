package com.edms.file_management.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/document/{documentId}")
    public ResponseEntity<Comment> createComment(@PathVariable Long documentId, @RequestBody Map<String, Object> payload) {
        Long userId = Long.parseLong(payload.get("userId").toString());
        String content = (String) payload.get("content");

        Comment newComment = commentService.addComment(documentId, userId, content);
        return ResponseEntity.ok(newComment);
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<Comment>> getCommentsByDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(commentService.getCommentsByDocumentId(documentId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> payload) {

        Long userId = Long.parseLong(payload.get("userId").toString());
        String content = (String) payload.get("content");

        try {
            Comment updated = commentService.updateComment(commentId, userId, content);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> payload) {

        Long userId = Long.parseLong(payload.get("userId").toString());

        try {
            commentService.deleteComment(commentId, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

}
