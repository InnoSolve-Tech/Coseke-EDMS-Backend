package com.edms.file_management.documentType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentTypeService {

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DocumentTypeMetadataValueRepository metadataValueRepository;

    public DocumentType createDocumentType(DocumentType documentType) {
        if (documentType.getMetadata() != null) {
            documentType.getMetadata().forEach(metadata -> metadata.setDocumentType(documentType));
        }
        return documentTypeRepository.save(documentType);
    }

    public List<DocumentType> getAllDocumentTypes() {
        return documentTypeRepository.findAll();
    }

    public Optional<DocumentType> getDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id);
    }

    public DocumentType updateDocumentType(Long id, DocumentType updatedDocumentType) {
        return documentTypeRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedDocumentType.getName());
                    existing.getMetadata().clear();
                    existing.getMetadata().addAll(updatedDocumentType.getMetadata());
                    updatedDocumentType.getMetadata().forEach(metadata -> metadata.setDocumentType(existing));
                    return documentTypeRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("DocumentType not found with id: " + id));
    }

    public DocumentType addMetadata(long documentTypeId, DocumentTypeMetadataValue metadataValue) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new RuntimeException("DocumentType not found with id: " + documentTypeId));

        metadataValue.setDocumentType(documentType);
        metadataValueRepository.save(metadataValue);

        return documentType;
    }

    public DocumentType updateMetadata(long documentTypeId, long metadataId, DocumentTypeMetadataValue metadataValue) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new RuntimeException("DocumentType not found with id: " + documentTypeId));

        DocumentTypeMetadataValue existingMetadata = metadataValueRepository.findById(metadataId)
                .orElseThrow(() -> new RuntimeException("Metadata not found with id: " + metadataId));

        existingMetadata.setName(metadataValue.getName());
        existingMetadata.setType(metadataValue.getType());
        existingMetadata.setValue(metadataValue.getValue());
        existingMetadata.setOptions(metadataValue.getOptions());

        metadataValueRepository.save(existingMetadata);

        return documentType;
    }

    public DocumentType deleteMetadata(long documentTypeId, long metadataId) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new RuntimeException("DocumentType not found with id: " + documentTypeId));

        DocumentTypeMetadataValue metadataValue = metadataValueRepository.findById(metadataId)
                .orElseThrow(() -> new RuntimeException("Metadata not found with id: " + metadataId));

        metadataValueRepository.delete(metadataValue);

        return documentType;
    }

    public void deleteDocumentType(Long id) {
        if (!documentTypeRepository.existsById(id)) {
            throw new RuntimeException("DocumentType not found with id: " + id);
        }
        documentTypeRepository.deleteById(id);
    }
}
