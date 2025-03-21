package com.edms.file_management.version;

import com.edms.file_management.filemanager.FileManager;
import com.edms.file_management.version.dto.CreateVersionFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/version-files")
@RequiredArgsConstructor
public class VersionFileController {

    private final VersionFileService versionFileService;

    @PostMapping("/upload")
    public ResponseEntity<VersionFile> uploadVersionFile(
            @RequestPart("fileData") CreateVersionFileDTO dto,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("userId") Long userId) {
        try {
            VersionFile versionFile = versionFileService.uploadVersionFile(dto, file, userId);
            return new ResponseEntity<>(versionFile, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/upload-version/{documentId}")
    public ResponseEntity<String> uploadVersionFile(
            @PathVariable Long documentId,
            @RequestParam("versionType") VersionType versionType,
            @RequestPart("fileData") CreateVersionFileDTO fileData,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("userId") Long userId
    ) {
        try {
            fileData.setDocumentId(documentId);
            fileData.setVersionType(versionType);
            versionFileService.uploadVersionFile(fileData, file, userId);
            return ResponseEntity.ok("Version file uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload version file: " + e.getMessage());
        }
    }

}
