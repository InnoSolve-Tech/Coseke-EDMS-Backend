package com.edms.forms.FormRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/form-records")
public class FormRecordController {

    private final FormRecordService formRecordService;

    @Autowired
    public FormRecordController(FormRecordService formRecordService) {
        this.formRecordService = formRecordService;
    }

    // Create a new form record
    @PostMapping
    public ResponseEntity<FormRecord> createFormRecord(@RequestBody FormRecord formRecord) {
        FormRecord savedFormRecord = formRecordService.saveFormRecord(formRecord);
        return new ResponseEntity<>(savedFormRecord, HttpStatus.CREATED);
    }

    // Get all form records
    @GetMapping
    public ResponseEntity<List<FormRecord>> getAllFormRecords() {
        List<FormRecord> formRecords = formRecordService.getAllFormRecords();
        return new ResponseEntity<>(formRecords, HttpStatus.OK);
    }

    // Get form record by ID
    @GetMapping("/{id}")
    public ResponseEntity<FormRecord> getFormRecordById(@PathVariable Long id) {
        Optional<FormRecord> formRecord = formRecordService.getFormRecordById(id);
        return formRecord.map(record -> new ResponseEntity<>(record, HttpStatus.OK))
                         .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get form record by ID
    @GetMapping("/form/{id}")
    public ResponseEntity<List<FormRecord>> getFormRecordByForm(@PathVariable Long id) {
        List<FormRecord> formRecords = formRecordService.getFormRecordByForm(id);
        return new ResponseEntity<>(formRecords, HttpStatus.OK);
    }

    // Delete form record by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormRecord(@PathVariable Long id) {
        formRecordService.deleteFormRecord(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Get form records by userId (Optional, if needed)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FormRecord>> getFormRecordsByUserId(@PathVariable Long userId) {
        List<FormRecord> formRecords = formRecordService.getFormRecordsByUserId(userId);
        return new ResponseEntity<>(formRecords, HttpStatus.OK);
    }
}

