package com.edms.file_management.fileVersions;

import com.edms.file_management.filemanager.FileManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileVersionsRepository extends JpaRepository<FileVersions, Long> {
    Optional<FileVersions> findByVersionName(String versionName);
}
