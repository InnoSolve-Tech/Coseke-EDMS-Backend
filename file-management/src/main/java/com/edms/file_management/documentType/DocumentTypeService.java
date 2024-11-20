package com.edms.file_management.documentType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentTypeService {

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    // Create a new document type
    public DocumentType createDocumentType(DocumentType documentType) {
        return documentTypeRepository.save(documentType);
    }

    // Get all document types
    public List<DocumentType> getAllDocumentTypes() {
        return documentTypeRepository.findAll();
    }

    // Get a document type by ID
    public Optional<DocumentType> getDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id);
    }

    // Update a document type
    public DocumentType updateDocumentType(Long id, DocumentType updatedDocumentType) {
        return documentTypeRepository.findById(id)
                .map(existing -> {
                    existing.setDocumentType(updatedDocumentType.getDocumentType());
                    existing.setMetadata(updatedDocumentType.getMetadata());
                    existing.setFolderId(updatedDocumentType.getFolderId());
                    return documentTypeRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("DocumentType not found with id: " + id));
    }

    // Delete a document type by ID
    public void deleteDocumentType(Long id) {
        if (!documentTypeRepository.existsById(id)) {
            throw new RuntimeException("DocumentType not found with id: " + id);
        }
        documentTypeRepository.deleteById(id);
    }
}
