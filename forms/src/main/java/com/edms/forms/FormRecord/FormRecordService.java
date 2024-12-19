package com.edms.forms.FormRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edms.forms.Form.Form;
import com.edms.forms.Form.FormRepository;
import com.edms.forms.FormField.FormFieldRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FormRecordService {

    private final FormRepository formRepository;
    private final FormRecordRepository formRecordRepository;
    private final FormFieldRepository formFieldRepository;

    @Autowired
    public FormRecordService(FormRecordRepository formRecordRepository, FormFieldRepository formFieldRepository, FormRepository formRepository) {
        this.formRecordRepository = formRecordRepository;
        this.formFieldRepository = formFieldRepository;
        this.formRepository = formRepository;
    }

    // Create or Update FormRecord
    public FormRecord saveFormRecord(FormRecord formRecord) {
        // Reset the FormField for each FormFieldValue
        formRecord.getFormFieldValues().forEach(fieldValue -> {
            if (fieldValue.getFormField() != null && fieldValue.getFormField().getId() != null) {
                fieldValue.setFormField(formFieldRepository.findById(fieldValue.getFormField().getId())
                        .orElseThrow(() -> new IllegalArgumentException("FormField not found with ID: " + fieldValue.getFormField().getId())));
            }
        });

        return formRecordRepository.save(formRecord);
    }


    // Get all form records
    public List<FormRecord> getAllFormRecords() {
        return formRecordRepository.findAll();
    }

    // Get form record by ID
    public Optional<FormRecord> getFormRecordById(Long id) {
        return formRecordRepository.findById(id);
    }

      // Get form record by Form ID
      public List<FormRecord> getFormRecordByForm(Long id) {
        Form form = formRepository.findById(id).orElseThrow();
        return formRecordRepository.findByForm(form);
    }

    // Delete form record by ID
    public void deleteFormRecord(Long id) {
        formRecordRepository.deleteById(id);
    }

    // You can add more service methods like finding records by userId or formId
    public List<FormRecord> getFormRecordsByUserId(Long userId) {
        // Custom query logic can be added in the repository, if necessary
        return formRecordRepository.findByUserId(userId);
    }
}

