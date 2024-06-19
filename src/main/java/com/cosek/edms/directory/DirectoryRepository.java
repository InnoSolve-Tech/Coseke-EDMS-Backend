package com.cosek.edms.directory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    @Query(value="select top 1 * from Folder order by FolderID desc",nativeQuery = true)
    List<Directory> findByLastInput();
}
