package com.cosek.edms.filemanager;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileManagerRepository extends JpaRepository<FileManager, Long> {
    Optional<FileManager> findByHashName(String hashName);
    Optional<List<FileManager>> findByFolderID(Long folderID);

    @Query("SELECT f FROM FileManager f WHERE f.filename LIKE %:keyword% OR f.documentName LIKE %:keyword%")
    Optional<List<FileManager>> searchFiles(@Param("keyword") String keyword);
    
}
