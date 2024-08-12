package com.cosek.edms.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchRepository extends JpaRepository<Search, Long> {

    @Query(value="select id,folderid,document_name,document_type,last_modified_date, created_date, file_link,filename, mime_type from file_manager where filename LIKE %:keyword% OR document_name LIKE %:keyword%",nativeQuery=true)
    Optional<List<Search>> searchFiles(@Param("keyword") String keyword);

    
}
