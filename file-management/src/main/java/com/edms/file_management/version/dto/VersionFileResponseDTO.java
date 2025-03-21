package com.edms.file_management.version.dto;

import com.edms.file_management.version.VersionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VersionFileResponseDTO {
    private Long id;
    private String originalName;
    private String fileUrl;
    private String changes;
    private LocalDateTime uploadedAt;
    private VersionType versionType;
    private Long documentId;
}