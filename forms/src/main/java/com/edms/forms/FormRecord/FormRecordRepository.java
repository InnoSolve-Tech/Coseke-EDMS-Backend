package com.edms.forms.FormRecord;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormRecordRepository extends JpaRepository<FormRecord, Long> {
    // You can define custom queries if needed. For example:
    List<FormRecord> findByUserId(Long userId);

    List<FormRecord> findByForm(Long formId);
}