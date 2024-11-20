package com.edms.file_management.documentType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/document-types")
public class DocumentTypeController {

    @Autowired
    private DocumentTypeService documentTypeService;

    // Create a new document type
    @PostMapping("/create")
    public ResponseEntity<DocumentType> createDocumentType(@RequestBody DocumentType documentType) {
        return ResponseEntity.ok(documentTypeService.createDocumentType(documentType));
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

    // Update a document type
    @PutMapping("/update/{id}")
    public ResponseEntity<DocumentType> updateDocumentType(
            @PathVariable Long id,
            @RequestBody DocumentType updatedDocumentType) {
        try {
            return ResponseEntity.ok(documentTypeService.updateDocumentType(id, updatedDocumentType));
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
