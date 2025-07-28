package com.edms.file_management.directoryAccessControl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectoryAccessControlRepository extends JpaRepository<DirectoryAccessControl, Long> {
}
