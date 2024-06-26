package com.cosek.edms.filemanager;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileManagerRepository extends JpaRepository<FileManager, Long> {
    Optional<FileManager> findByHashName(String hashName);
    Optional<List<FileManager>> findByFolderID(Long folderID);
    
}
