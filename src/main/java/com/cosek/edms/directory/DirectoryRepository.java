package com.cosek.edms.directory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
     @Query(value = "SELECT * FROM Directory ORDER BY FolderID DESC", nativeQuery = true)
    List<Directory> findByLastInput();

    @Query(value = "SELECT * FROM Directory WHERE Name = :name", nativeQuery = true)
    Optional<Directory> findByName(String name);
}
