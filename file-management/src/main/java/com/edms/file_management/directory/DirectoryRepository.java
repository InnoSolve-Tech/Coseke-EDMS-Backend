package com.edms.file_management.directory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
     @Query(value = "SELECT * FROM Directory ORDER BY FolderID DESC", nativeQuery = true)
    List<Directory> findByLastInput();

    @Query(value = "SELECT * FROM Directory WHERE Name = :name", nativeQuery = true)
    Optional<Directory> findByName(String name);

    List<Directory> findByParentFolderID(int parentFolderID);

}
