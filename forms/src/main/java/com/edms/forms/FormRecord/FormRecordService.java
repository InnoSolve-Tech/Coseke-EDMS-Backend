package com.edms.forms.FormRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Create a new FormRecord
    @Transactional
    public FormRecord createFormRecord(FormRecord formRecord) {
        // Validate Form
        Form form = formRepository.findById(formRecord.getForm().getId())
                .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formRecord.getForm().getId()));
        formRecord.setForm(form);

        // Validate and set FormField for each FormFieldValue
        formRecord.getFormFieldValues().forEach(fieldValue -> {
            if (fieldValue.getFormField() != null && fieldValue.getFormField().getId() != null) {
                fieldValue.setFormRecord(formRecord);
                fieldValue.setFormField(formFieldRepository.findById(fieldValue.getFormField().getId())
                        .orElseThrow(() -> new IllegalArgumentException("FormField not found with ID: " + fieldValue.getFormField().getId())));
            }
        });

        // Save the new FormRecord
        return formRecordRepository.save(formRecord);
    }

    // Update an existing FormRecord
    @Transactional
    public FormRecord updateFormRecord(Long id, FormRecord updatedFormRecord) {
        // Find the existing FormRecord
        FormRecord existingFormRecord = formRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FormRecord not found with ID: " + id));
    
        // Validate Form
        Form form = formRepository.findById(updatedFormRecord.getForm().getId())
                .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + updatedFormRecord.getForm().getId()));
        existingFormRecord.setForm(form);
    
        // Update the existing FormRecord's formFieldValues in place
        existingFormRecord.getFormFieldValues().clear(); // Clear the existing collection
        existingFormRecord.getFormFieldValues().addAll(updatedFormRecord.getFormFieldValues()); // Add the updated values
    
        // Validate and set FormField for each FormFieldValue
        existingFormRecord.getFormFieldValues().forEach(fieldValue -> {
            if (fieldValue.getFormField() != null && fieldValue.getFormField().getId() != null) {
                fieldValue.setFormRecord(existingFormRecord);
                fieldValue.setFormField(formFieldRepository.findById(fieldValue.getFormField().getId())
                        .orElseThrow(() -> new IllegalArgumentException("FormField not found with ID: " + fieldValue.getFormField().getId())));
            }
        });
    
        // Save the updated FormRecord
        return formRecordRepository.save(existingFormRecord);
    }
    
    // Get all form records
    public List<FormRecord> getAllFormRecords() {
        return formRecordRepository.findAll();
    }

    // Get form record by ID
    public Optional<FormRecord> getFormRecordById(Long id) {
        return formRecordRepository.findById(id);
    }

    // Get form records by Form ID
    public List<FormRecord> getFormRecordByForm(Long formId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));
        return formRecordRepository.findByForm(form);
    }

    // Delete form record by ID
    public void deleteFormRecord(Long id) {
        formRecordRepository.deleteById(id);
    }

    // Get form records by User ID
    public List<FormRecord> getFormRecordsByUserId(Long userId) {
        return formRecordRepository.findByUserId(userId);
    }
}