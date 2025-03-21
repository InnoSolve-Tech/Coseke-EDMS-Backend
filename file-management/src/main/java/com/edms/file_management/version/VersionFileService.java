package com.edms.file_management.version;

import com.edms.file_management.filemanager.FileManager;
import com.edms.file_management.filemanager.FileManagerRepository;
import com.edms.file_management.helper.EncryptionUtil;
import com.edms.file_management.helper.HashUtil;
import com.edms.file_management.version.dto.CreateVersionFileDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VersionFileService {

    private final FileManagerRepository fileManagerRepository;
    private final VersionFileRepository versionFileRepository;

    @Value("${storage.path}")
    private String rootLocation;

    public VersionFile uploadVersionFile(CreateVersionFileDTO dto, MultipartFile file, Long userId) throws Exception {
        if (file.isEmpty()) throw new Exception("Empty file");

        FileManager document = fileManagerRepository.findById(dto.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        String hash = HashUtil.generateHash(file.getOriginalFilename(), LocalDateTime.now());
        Path destinationFile = Paths.get(rootLocation)
                .resolve(hash + getFileExtension(file.getOriginalFilename()))
                .normalize()
                .toAbsolutePath();

        Files.createDirectories(destinationFile.getParent());

        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(destinationFile)) {
            EncryptionUtil.encrypt(inputStream, outputStream);
        }

        VersionFile versionFile = VersionFile.builder()
                .originalName(file.getOriginalFilename())
                .hashName(hash)
                .mimeType(file.getContentType())
                .fileUrl("/files/download/" + hash)
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(userId)
                .changes(dto.getChanges())
                .versionType(dto.getVersionType()) // Assuming you added this to the DTO
                .document(document)
                .build();

        return versionFileRepository.save(versionFile);
    }


    private String getFileExtension(String filename) {
        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : "";
    }
}

