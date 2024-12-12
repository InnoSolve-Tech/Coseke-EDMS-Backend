package com.edms.forms.FormRecords;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edms.forms.FormCreation.FormCreation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FormRecordsRepository extends JpaRepository<FormRecords, Long> {
    List<FormRecords> findByFormCreation(FormCreation formCreation);

    List<FormRecords> findBySubmissionDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}