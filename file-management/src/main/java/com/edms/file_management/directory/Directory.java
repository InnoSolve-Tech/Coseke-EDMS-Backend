package com.edms.file_management.directory;

import com.edms.file_management.filemanager.FileManager;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Directory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int folderID;

    private String name;

    @Column(name = "parent_folderid") // Maps to the database column
    private int parentFolderID;

    private long documentTypeID;

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

    @Override
    public String toString() {
        return "Directory [folderID=" + folderID + ", name=" + name + ", parentFolderID=" + parentFolderID + "]";
    }

    @JsonManagedReference
    @OneToMany(mappedBy = "directory", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileManager> files = new ArrayList<>();
}
