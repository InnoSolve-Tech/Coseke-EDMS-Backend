package com.cosek.edms.filemanager;

import com.cosek.edms.ActiveWorkflows.ActiveWorkflows;
import com.cosek.edms.WorkflowComments.WorkflowComments;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.cosek.edms.helper.JsonMapConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

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

    private Long folderID;
    private String filename;
    private String documentType;
    private String documentName;
    private String hashName;
    private String fileLink;
    private String mimeType;
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "nvarchar(max)")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(name = "createdDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name="lastModifiedDate", nullable = true)
    private LocalDateTime lastModifiedDateTime;

    @LastModifiedBy
    @Column(name = "lastModifiedBy", nullable = true)
    private Long lastModifiedBy;

    @CreatedBy
    @Column(name="createdBy", nullable = false, updatable = false)
    private Long createdBy;

    @OneToMany(cascade = CascadeType.DETACH)
    @Column(name = "fileManager")
    private Collection<ActiveWorkflows> activeWorkflows;

}
