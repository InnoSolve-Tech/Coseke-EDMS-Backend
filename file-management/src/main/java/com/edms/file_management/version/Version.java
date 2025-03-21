package com.edms.file_management.version;

import com.edms.file_management.filemanager.FileManager;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String versionName;
    private String changes;
    private String fileUrl;

    @CreatedDate
    @Column(name = "createdDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(name = "createdBy", nullable = false, updatable = false)
    private Long createdBy;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentId")
    private FileManager document;

    @OneToMany(mappedBy = "version", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VersionComment> versionComments;

    @Enumerated(EnumType.STRING)
    private VersionType versionType;

}