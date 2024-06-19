package com.cosek.edms.filemanager;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileManagerRepository extends JpaRepository<FileManager, Long> {
    
}
