package com.edms.file_management.documentType;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/document-types")
public class DocumentTypeController {

    @Autowired
    private DocumentTypeService documentTypeService;

    @PostMapping("/create")
    public ResponseEntity<DocumentType> createDocumentType(@RequestBody DocumentType documentType) {
        DocumentType savedDocumentType = documentTypeService.createDocumentType(documentType);
        return ResponseEntity.ok(savedDocumentType);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DocumentType>> getAllDocumentTypes() {
        List<DocumentType> documentTypes = documentTypeService.getAllDocumentTypes();
        return ResponseEntity.ok(documentTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentType> getDocumentTypeById(@PathVariable Long id) {
        return documentTypeService.getDocumentTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentType> updateDocumentType(@PathVariable Long id, @RequestBody DocumentType updatedDocumentType) {
        try {
            return ResponseEntity.ok(documentTypeService.updateDocumentType(id, updatedDocumentType));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/add-metadata")
    public ResponseEntity<DocumentType> addMetadata(@PathVariable long id, @RequestBody DocumentTypeMetadataValue metadataValue) {
        try {
            return ResponseEntity.ok(documentTypeService.addMetadata(id, metadataValue));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/update-metadata/{metadataId}")
    public ResponseEntity<DocumentType> updateMetadata(@PathVariable long id, @PathVariable long metadataId, @RequestBody DocumentTypeMetadataValue metadataValue) {
        try {
            return ResponseEntity.ok(documentTypeService.updateMetadata(id, metadataId, metadataValue));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/delete-metadata/{metadataId}")
    public ResponseEntity<DocumentType> deleteMetadata(@PathVariable long id, @PathVariable long metadataId) {
        try {
            return ResponseEntity.ok(documentTypeService.deleteMetadata(id, metadataId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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