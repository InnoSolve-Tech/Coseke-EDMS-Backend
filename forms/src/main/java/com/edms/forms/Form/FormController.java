package com.edms.forms.Form;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
public class FormController {
    private final FormService formService;

    @PostMapping
    public ResponseEntity<FormDto> createForm(@RequestBody FormDto formDto) {
        FormDto createdForm = formService.createForm(formDto);
        return new ResponseEntity<>(createdForm, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FormDto>> getAllForms() {
        List<FormDto> forms = formService.getAllForms();
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormDto> getFormById(@PathVariable Long id) {
        FormDto form = formService.getFormById(id);
        return ResponseEntity.ok(form);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormDto> updateForm(
            @PathVariable Long id, 
            @RequestBody FormDto formDto
    ) {
        FormDto updatedForm = formService.updateForm(id, formDto);
        return ResponseEntity.ok(updatedForm);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return ResponseEntity.noContent().build();
    }
}
