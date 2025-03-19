package com.edms.file_management.version;

import com.edms.file_management.version.dto.CreateVersionCommentDTO;
import com.edms.file_management.version.dto.UpdateVersionCommentDTO;
import com.edms.file_management.version.dto.VersionCommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/version-comments")
@RequiredArgsConstructor
public class VersionCommentController {

    private final VersionCommentService versionCommentService;

    @GetMapping("/version/{versionId}")
    public ResponseEntity<List<VersionCommentDTO>> getAllCommentsForVersion(@PathVariable Long versionId) {
        List<VersionCommentDTO> comments = versionCommentService.getAllCommentsForVersion(versionId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VersionCommentDTO> getCommentById(@PathVariable Long id) {
        VersionCommentDTO comment = versionCommentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @PostMapping
    public ResponseEntity<VersionCommentDTO> createComment(
            @RequestBody CreateVersionCommentDTO createVersionCommentDTO,
            @RequestHeader("userId") Long userId) {
        VersionCommentDTO createdComment = versionCommentService.createComment(createVersionCommentDTO, userId);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VersionCommentDTO> updateComment(
            @PathVariable Long id,
            @RequestBody UpdateVersionCommentDTO updateVersionCommentDTO,
            @RequestHeader("userId") Long userId) {
        VersionCommentDTO updatedComment = versionCommentService.updateComment(id, updateVersionCommentDTO, userId);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        versionCommentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
}