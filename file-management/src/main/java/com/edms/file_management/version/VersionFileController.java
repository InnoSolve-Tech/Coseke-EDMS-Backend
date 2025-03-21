package com.edms.file_management.version;

import com.edms.file_management.version.dto.CreateVersionFileDTO;
import com.edms.file_management.version.dto.VersionFileResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/version-files")
@RequiredArgsConstructor
public class VersionFileController {

    private static final Logger logger = LoggerFactory.getLogger(VersionFileController.class);
    private final VersionFileService versionFileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVersionFile(
            @RequestPart("fileData") CreateVersionFileDTO dto,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("userId") Long userId) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("File cannot be empty");
            }

            VersionFileResponseDTO response = versionFileService.uploadVersionFile(dto, file, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error uploading version file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload version file: " + e.getMessage());
        }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<?> getVersionsByDocumentId(@PathVariable Long documentId) {
        try {
            List<VersionFileResponseDTO> versions = (List<VersionFileResponseDTO>) versionFileService.getVersionsByDocumentId(documentId);
            return ResponseEntity.ok(versions);
        } catch (Exception e) {
            logger.error("Error retrieving versions for document ID: " + documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve versions: " + e.getMessage());
        }
    }
}