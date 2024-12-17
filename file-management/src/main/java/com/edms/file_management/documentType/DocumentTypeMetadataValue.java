package com.edms.file_management.documentType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentTypeMetadataValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type; // e.g., "text" or "select"

    @Column(name = "value")
    private String value; // For text type

    @ElementCollection
    @CollectionTable(name = "metadata_options", joinColumns = @JoinColumn(name = "metadata_id"))
    @Column(name = "option_value")
    private List<String> options; // For select type

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;
}
