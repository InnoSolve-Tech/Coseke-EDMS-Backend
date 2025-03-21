package com.edms.file_management.version;

import com.edms.file_management.filemanager.FileManager;
import com.edms.file_management.filemanager.FileManagerRepository;
import com.edms.file_management.helper.EncryptionUtil;
import com.edms.file_management.helper.HashUtil;
import com.edms.file_management.version.dto.CreateVersionFileDTO;
import com.edms.file_management.version.dto.VersionFileResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class VersionFileService {

    private final FileManagerRepository fileManagerRepository;
    private final VersionFileRepository versionFileRepository;

    @Value("${storage.path}")
    private String rootLocation;

    @Transactional
    public VersionFileResponseDTO uploadVersionFile(CreateVersionFileDTO dto, MultipartFile file, Long userId) throws Exception {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        // Find the parent document
        FileManager document = findDocumentById(dto.getDocumentId());

        // Process and store the file
        String hash = HashUtil.generateHash(file.getOriginalFilename(), LocalDateTime.now());
        String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));

        // Store encrypted file
        storeEncryptedFile(file, hash, fileExtension);

        // Create and save the version entity
        VersionFile versionFile = createVersionFileEntity(dto, file, hash, document, userId);
        VersionFile savedVersion = versionFileRepository.save(versionFile);

        // Return DTO with necessary information
        return mapToResponseDTO(savedVersion);
    }

    public Iterable<VersionFileResponseDTO> getVersionsByDocumentId(Long documentId) {
        // Find the parent document to ensure it exists
        FileManager document = findDocumentById(documentId);

        // Retrieve all versions for this document
        List<VersionFile> versions = versionFileRepository.findByDocumentIdOrderByUploadedAtDesc(documentId);

        // Map to response DTOs
        return versions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private FileManager findDocumentById(Long documentId) {
        return fileManagerRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with ID: " + documentId));
    }

    private void storeEncryptedFile(MultipartFile file, String hash, String fileExtension) throws Exception {
        Path destinationFile = Paths.get(rootLocation)
                .resolve(hash + fileExtension)
                .normalize()
                .toAbsolutePath();

        Files.createDirectories(destinationFile.getParent());

        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(destinationFile)) {
            EncryptionUtil.encrypt(inputStream, outputStream);
        }
    }

    private VersionFile createVersionFileEntity(CreateVersionFileDTO dto, MultipartFile file, String hash, FileManager document, Long userId) {
        return VersionFile.builder()
                .originalName(file.getOriginalFilename())
                .hashName(hash)
                .mimeType(file.getContentType())
                .fileUrl("/files/download/" + hash)
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(userId)
                .changes(dto.getChanges())
                .versionType(dto.getVersionType())
                .document(document)
                .build();
    }

    private VersionFileResponseDTO mapToResponseDTO(VersionFile versionFile) {
        return VersionFileResponseDTO.builder()
                .id(versionFile.getId())
                .originalName(versionFile.getOriginalName())
                .fileUrl(versionFile.getFileUrl())
                .changes(versionFile.getChanges())
                .uploadedAt(versionFile.getUploadedAt())
                .versionType(versionFile.getVersionType())
                .documentId(versionFile.getDocument().getId())
                .build();
    }

    private String getFileExtension(String filename) {
        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : "";
    }
}