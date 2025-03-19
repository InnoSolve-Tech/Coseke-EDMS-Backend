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
public class VersionCommentDTO {
    private Long id;
    private String content;
    private LocalDateTime createdDate;
    private Long createdBy;
    private Long versionId;
}

