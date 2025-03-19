package com.edms.file_management.version;

import com.edms.file_management.version.dto.CreateVersionCommentDTO;
import com.edms.file_management.version.dto.UpdateVersionCommentDTO;
import com.edms.file_management.version.dto.VersionCommentDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VersionCommentService {

    private final VersionCommentRepository versionCommentRepository;
    private final VersionRepository versionRepository;

    @Transactional(readOnly = true)
    public List<VersionCommentDTO> getAllCommentsForVersion(Long versionId) {
        List<VersionComment> comments = versionCommentRepository.findByVersionId(versionId);
        return comments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VersionCommentDTO getCommentById(Long id) {
        VersionComment comment = versionCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));
        return mapToDTO(comment);
    }

    @Transactional
    public VersionCommentDTO createComment(CreateVersionCommentDTO createVersionCommentDTO, Long userId) {
        Version version = versionRepository.findById(createVersionCommentDTO.getVersionId())
                .orElseThrow(() -> new EntityNotFoundException("Version not found with id: " + createVersionCommentDTO.getVersionId()));

        VersionComment comment = VersionComment.builder()
                .content(createVersionCommentDTO.getContent())
                .version(version)
                .createdBy(userId)
                .createdDate(LocalDateTime.now())
                .build();

        VersionComment savedComment = versionCommentRepository.save(comment);
        return mapToDTO(savedComment);
    }

    @Transactional
    public VersionCommentDTO updateComment(Long id, UpdateVersionCommentDTO updateVersionCommentDTO, Long userId) {
        VersionComment comment = versionCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));

        // Ensure only the comment creator can update it
        if (!comment.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to update this comment");
        }

        if (updateVersionCommentDTO.getContent() != null) {
            comment.setContent(updateVersionCommentDTO.getContent());
        }

        VersionComment updatedComment = versionCommentRepository.save(comment);
        return mapToDTO(updatedComment);
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        VersionComment comment = versionCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));

        // Ensure only the comment creator can delete it
        if (!comment.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to delete this comment");
        }

        versionCommentRepository.deleteById(id);
    }

    private VersionCommentDTO mapToDTO(VersionComment comment) {
        return VersionCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdDate(comment.getCreatedDate())
                .createdBy(comment.getCreatedBy())
                .versionId(comment.getVersion().getId())
                .build();
    }
}