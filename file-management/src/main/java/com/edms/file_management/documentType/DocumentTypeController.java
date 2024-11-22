package com.edms.file_management.documentType;

import com.edms.file_management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/document-types")
public class DocumentTypeController {

    @Autowired
    private DocumentTypeService documentTypeService;

    // Create a new document type
    @PostMapping("/create")
    public ResponseEntity<DocumentType> createDocumentType(@RequestBody DocumentType documentType) {
        DocumentType savedDocumentType = documentTypeService.createDocumentType(documentType);
        return ResponseEntity.ok(savedDocumentType);
    }

    // Get all document types
    @GetMapping("/all")
    public ResponseEntity<List<DocumentType>> getAllDocumentTypes() {
        return ResponseEntity.ok(documentTypeService.getAllDocumentTypes());
    }

    // Get a document type by ID
    @GetMapping("/{id}")
    public ResponseEntity<DocumentType> getDocumentTypeById(@PathVariable Long id) {
        return documentTypeService.getDocumentTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a document type's basic information
//    @PutMapping("/update/{id}")
//    public ResponseEntity<DocumentType> updateDocumentType(
//            @PathVariable Long id,
//            @RequestBody DocumentType updatedDocumentType) {
//        try {
//            return ResponseEntity.ok(documentTypeService.updateDocumentType(id, updatedDocumentType));
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }

    // Add a metadata entry to a document type
    @PostMapping("/{id}/add-metadata")
    public ResponseEntity<DocumentType> addMetadata(
            @PathVariable long id,
            @RequestBody DocumentTypeMetadataValue metadataValue) {
        try {
            return ResponseEntity.ok(documentTypeService.addMetadata(id, metadataValue));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update metadata entry
    @PutMapping("/{id}/update-metadata/{metadataId}")
    public ResponseEntity<DocumentType> updateMetadata(
            @PathVariable long id,
            @PathVariable long metadataId,
            @RequestBody DocumentTypeMetadataValue metadataValue) {
        try {
            return ResponseEntity.ok(documentTypeService.updateMetadata(id, metadataId, metadataValue));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a metadata entry
    @DeleteMapping("/{id}/delete-metadata/{metadataId}")
    public ResponseEntity<DocumentType> deleteMetadata(
            @PathVariable long id,
            @PathVariable long metadataId) {
        try {
            return ResponseEntity.ok(documentTypeService.deleteMetadata(id, metadataId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a document type by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDocumentType(@PathVariable Long id) {
        try {
            documentTypeService.deleteDocumentType(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
