package com.edms.file_management.documentType;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "documentType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentTypeMetadataValue> metadata;
}
