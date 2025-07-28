package com.edms.file_management.filemanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileManagerRepository extends JpaRepository<FileManager, Long> {
    Optional<FileManager> findByHashName(String hashName);
    Optional<List<FileManager>> findByFolderID(Long folderID);

    @Query("SELECT f FROM FileManager f WHERE f.filename LIKE %:keyword% OR f.documentName LIKE %:keyword%")
    Optional<List<FileManager>> searchFiles(@Param("keyword") String keyword);
    
}
