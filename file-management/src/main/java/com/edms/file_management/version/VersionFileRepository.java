package com.edms.file_management.version;

import com.edms.file_management.version.VersionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionFileRepository extends JpaRepository<VersionFile, Long> {
    List<VersionFile> findByDocument_Id(Long documentId);
}
