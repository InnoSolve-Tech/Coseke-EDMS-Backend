package com.edms.file_management.documentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeMetadataValueRepository extends JpaRepository<DocumentTypeMetadataValue, Long> {
}
