package com.edms.file_management.documentType;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@EntityListeners(AuditingEntityListener.class)
//@JsonIgnoreProperties(value = {
//        "createdDate", "lastModifiedDateTime", "lastModifiedBy", "createdBy"
//}, allowGetters = true)
//public class DocumentType {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "document_type", nullable = false)
//    private String documentType;
//
//    @JsonManagedReference
//    @OneToMany(mappedBy = "documentType", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<DocumentTypeMetadataValue> metadata = new ArrayList<>();
//
//    @CreatedDate
//    @Column(name = "created_date", nullable = false, updatable = false)
//    private LocalDateTime createdDate;
//
//    @LastModifiedDate
//    @Column(name = "last_modified_date")
//    private LocalDateTime lastModifiedDateTime;
//
//    @LastModifiedBy
//    @Column(name = "last_modified_by")
//    private Long lastModifiedBy;
//
//    @CreatedBy
//    @Column(name = "created_by", nullable = false, updatable = false)
//    private Long createdBy;
//
//    // Helper method to manage bidirectional relationship
//    public void addMetadata(DocumentTypeMetadataValue metadataValue) {
//        metadata.add(metadataValue);
//        metadataValue.setDocumentType(this);
//    }
//
//    public void removeMetadata(DocumentTypeMetadataValue metadataValue) {
//        metadata.remove(metadataValue);
//        metadataValue.setDocumentType(null);
//    }
//}

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "documentType", nullable = false)
    private String name;

    // Remove this line if it exists
    // @Column(name = "created_by", nullable = false)
    // private String createdBy;

    @OneToMany(mappedBy = "documentType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentTypeMetadataValue> metadata;
}
