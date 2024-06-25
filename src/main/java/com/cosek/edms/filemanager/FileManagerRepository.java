package com.cosek.edms.filemanager;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileManagerRepository extends JpaRepository<FileManager, Long> {
    Optional<FileManager> findByHashName(String hashName);
    
}
