package com.edms.file_management.filemanager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.edms.file_management.directory.Directory;
import com.edms.file_management.fileVersions.FileVersions;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.edms.file_management.helper.JsonMapConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer folderID;
    private String filename;
    private String documentType;
    private String documentName;
    private String hashName;
    private String mimeType;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "nvarchar(max)")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(name = "createdDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "lastModifiedDate", nullable = true)
    private LocalDateTime lastModifiedDateTime;

    @LastModifiedBy
    @Column(name = "lastModifiedBy", nullable = true)
    private Long lastModifiedBy;

    @CreatedBy
    @Column(name = "createdBy", nullable = false, updatable = false)
    private Long createdBy;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folderID", insertable = false, updatable = false)
    private Directory directory;

    @JsonManagedReference
    @OneToMany(mappedBy = "fileManager", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileVersions> fileVersions = new ArrayList<>();

    @JsonSetter("metadata")
    public void setMetadata(Object metadata) {
        if (metadata instanceof List) {
            this.metadata = ((List<Map<String, Object>>) metadata).stream()
                    .collect(Collectors.toMap(
                            map -> (String) map.get("name"),
                            map -> map.get("value")
                    ));
        } else if (metadata instanceof Map) {
            this.metadata = (Map<String, Object>) metadata;
        }
    }
}
