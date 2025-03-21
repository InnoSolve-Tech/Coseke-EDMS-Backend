package com.edms.file_management.version.dto;

import com.edms.file_management.version.VersionType;
import lombok.Data;

@Data
public class CreateVersionFileDTO {
    private Long documentId;
    private VersionType versionType;
    private String changes;
}
