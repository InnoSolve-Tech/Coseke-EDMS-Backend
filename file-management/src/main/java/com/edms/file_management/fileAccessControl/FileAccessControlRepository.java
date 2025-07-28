package com.edms.file_management.fileAccessControl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAccessControlRepository extends JpaRepository<FileAccessControl, Long> {
}
