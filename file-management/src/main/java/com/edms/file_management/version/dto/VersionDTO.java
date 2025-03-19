package com.edms.file_management.version.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDTO {
    private Long id;
    private String versionName;
    private String changes;
    private String fileUrl;
    private LocalDateTime createdDate;
    private Long createdBy;
    private String createdByUsername; // To be populated from the user service
    private Long documentId;
}

