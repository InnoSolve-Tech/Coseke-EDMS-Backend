package com.edms.file_management.version;

import com.edms.file_management.filemanager.FileManager;
import com.edms.file_management.filemanager.FileManagerRepository;
import com.edms.file_management.version.dto.CreateVersionDTO;
import com.edms.file_management.version.dto.UpdateVersionDTO;
import com.edms.file_management.version.dto.VersionDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;
    private final FileManagerRepository fileRepository;

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<VersionDTO>> getAllVersionsForDocument(@PathVariable Long documentId) {
        List<VersionDTO> versions = versionService.getAllVersionsForDocument(documentId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VersionDTO> getVersionById(@PathVariable Long id) {
        VersionDTO version = versionService.getVersionById(id);
        return ResponseEntity.ok(version);
    }

    @PostMapping
    public ResponseEntity<VersionDTO> createVersion(
            @RequestBody CreateVersionDTO createVersionDTO,
            @RequestHeader("userId") Long userId) {
        VersionDTO createdVersion = versionService.createVersion(createVersionDTO, userId);
        return new ResponseEntity<>(createdVersion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VersionDTO> updateVersion(
            @PathVariable Long id,
            @RequestBody UpdateVersionDTO updateVersionDTO,
            @RequestHeader("userId") Long userId) {
        VersionDTO updatedVersion = versionService.updateVersion(id, updateVersionDTO, userId);
        return ResponseEntity.ok(updatedVersion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVersion(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        versionService.deleteVersion(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/major")
    public ResponseEntity<VersionDTO> createMajorVersion(
            @RequestBody CreateVersionDTO dto,
            @RequestHeader("userId") Long userId) {
        dto.setVersionType(VersionType.MAJOR);
        FileManager storedFile = fileRepository.findByHashName(dto.getFileUrl())
                .orElseThrow(() -> new EntityNotFoundException("File not found for hash: " + dto.getFileUrl()));
        return new ResponseEntity<>(versionService.createVersionWithAutoVersionName(dto, userId, storedFile), HttpStatus.CREATED);
    }

    @PostMapping("/minor")
    public ResponseEntity<VersionDTO> createMinorVersion(
            @RequestBody CreateVersionDTO dto,
            @RequestHeader("userId") Long userId) {
        dto.setVersionType(VersionType.MINOR);
        FileManager storedFile = fileRepository.findByHashName(dto.getFileUrl())
                .orElseThrow(() -> new EntityNotFoundException("File not found for hash: " + dto.getFileUrl()));
        return new ResponseEntity<>(versionService.createVersionWithAutoVersionName(dto, userId, storedFile), HttpStatus.CREATED);
    }

}