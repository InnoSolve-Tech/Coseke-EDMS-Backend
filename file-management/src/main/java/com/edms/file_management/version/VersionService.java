package com.edms.file_management.version;

import com.edms.file_management.filemanager.FileManager;
import com.edms.file_management.filemanager.FileManagerRepository;
import com.edms.file_management.version.dto.CreateVersionDTO;
import com.edms.file_management.version.dto.UpdateVersionDTO;
import com.edms.file_management.version.dto.VersionDTO;
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
public class VersionService {

    private final VersionRepository versionRepository;
    private final FileManagerRepository fileManagerRepository;

    @Transactional(readOnly = true)
    public List<VersionDTO> getAllVersionsForDocument(Long documentId) {
        List<Version> versions = versionRepository.findByDocumentId(documentId);
        return versions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VersionDTO getVersionById(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Version not found with id: " + id));
        return mapToDTO(version);
    }

    @Transactional
    public VersionDTO createVersion(CreateVersionDTO createVersionDTO, Long userId) {
        FileManager document = fileManagerRepository.findById(createVersionDTO.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + createVersionDTO.getDocumentId()));

        Version version = Version.builder()
                .versionName(createVersionDTO.getVersionName())
                .changes(createVersionDTO.getChanges())
                .fileUrl(createVersionDTO.getFileUrl())
                .document(document)
                .createdBy(userId)
                .createdDate(LocalDateTime.now())
                .build();

        Version savedVersion = versionRepository.save(version);
        return mapToDTO(savedVersion);
    }

    @Transactional
    public VersionDTO updateVersion(Long id, UpdateVersionDTO updateVersionDTO, Long userId) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Version not found with id: " + id));

        // Ensure only the version creator can update it
        if (!version.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to update this version");
        }

        if (updateVersionDTO.getVersionName() != null) {
            version.setVersionName(updateVersionDTO.getVersionName());
        }
        if (updateVersionDTO.getChanges() != null) {
            version.setChanges(updateVersionDTO.getChanges());
        }
        if (updateVersionDTO.getFileUrl() != null) {
            version.setFileUrl(updateVersionDTO.getFileUrl());
        }

        Version updatedVersion = versionRepository.save(version);
        return mapToDTO(updatedVersion);
    }

    @Transactional
    public void deleteVersion(Long id, Long userId) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Version not found with id: " + id));

        // Ensure only the version creator can delete it
        if (!version.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to delete this version");
        }

        versionRepository.deleteById(id);
    }

    private VersionDTO mapToDTO(Version version) {
        return VersionDTO.builder()
                .id(version.getId())
                .versionName(version.getVersionName())
                .changes(version.getChanges())
                .fileUrl(version.getFileUrl())
                .createdDate(version.getCreatedDate())
                .createdBy(version.getCreatedBy())
                .documentId(version.getDocument().getId())
                .build();
    }
}