package com.edms.file_management.documentType;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
