package com.edms.file_management.version;

import com.edms.file_management.filemanager.FileManager;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class VersionFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String hashName;
    private String mimeType;
    private String fileUrl;
    private String changes;

    private LocalDateTime uploadedAt;
    private Long uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private FileManager document;

    @Enumerated(EnumType.STRING)
    private VersionType versionType;
}
