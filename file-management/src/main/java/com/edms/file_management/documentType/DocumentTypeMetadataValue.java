package com.edms.file_management.documentType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentTypeMetadataValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(name = "name", nullable = false)
    private String name; // Metadata key

    @Column(name = "type", nullable = false)
    private String type; // Metadata type (e.g., "string", "number")

    @Column(name = "value", nullable = false)
    private String value; // Metadata value
}
