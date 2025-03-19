package com.edms.file_management.version.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVersionDTO {
    private String versionName;
    private String changes;
    private String fileUrl;
}
