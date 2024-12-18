package com.edms.forms.Form;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edms.forms.FormField.FormField;
import com.edms.forms.SelectOption.SelectOption;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {
    private final FormRepository formRepository;

    @Transactional
    public FormDto createForm(FormDto formDto) {
        // Convert DTO to Entity
        Form formEntity = convertToEntity(formDto);
        
        // Save the form
        Form savedForm = formRepository.save(formEntity);
        
        // Convert back to DTO and return
        return convertToDto(savedForm);
    }

    @Transactional(readOnly = true)
    public List<FormDto> getAllForms() {
        return formRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FormDto getFormById(Long id) {
        return formRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Form not found with id: " + id));
    }

    @Transactional
    public FormDto updateForm(Long id, FormDto formDto) {
        // Find existing form
        Form existingForm = formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found with id: " + id));
        
        // Update form details
        existingForm.setName(formDto.getName());
        existingForm.setDescription(formDto.getDescription());
        
        // Clear and update form fields
        existingForm.getFormFields().clear();
        existingForm.getFormFields().addAll(
            formDto.getFormFields().stream()
                .map(this::convertToFormField)
                .collect(Collectors.toList())
        );
        
        // Update select options if present
        if (formDto.getSelectOptions() != null) {
            SelectOption selectOptions = new SelectOption();
            selectOptions.setOptions(formDto.getSelectOptions().getOptions());
            existingForm.setSelectOptions(selectOptions);
        }
        
        // Save and return
        return convertToDto(formRepository.save(existingForm));
    }

    @Transactional
    public void deleteForm(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found with id: " + id));
        
        formRepository.delete(form);
    }

    // Conversion methods
    private Form convertToEntity(FormDto formDto) {
        return Form.builder()
                .id(formDto.getId())
                .name(formDto.getName())
                .description(formDto.getDescription())
                .formFields(
                    formDto.getFormFields().stream()
                        .map(this::convertToFormField)
                        .collect(Collectors.toList())
                )
                .selectOptions(
                    formDto.getSelectOptions() != null 
                        ? SelectOption.builder()
                            .options(formDto.getSelectOptions().getOptions())
                            .build() 
                        : null
                )
                .build();
    }

    private FormDto convertToDto(Form formEntity) {
        return FormDto.builder()
                .id(formEntity.getId())
                .name(formEntity.getName())
                .description(formEntity.getDescription())
                .formFields(
                    formEntity.getFormFields().stream()
                        .map(this::convertToFormFieldDto)
                        .collect(Collectors.toList())
                )
                .selectOptions(
                    formEntity.getSelectOptions() != null
                        ? FormDto.SelectOptionsDto.builder()
                            .options(formEntity.getSelectOptions().getOptions())
                            .build()
                        : null
                )
                .build();
    }

    private FormField convertToFormField(FormDto.FormFieldDto fieldDto) {
        return FormField.builder()
                .id(fieldDto.getId())
                .name(fieldDto.getName())
                .type(fieldDto.getType())
                .selectOptions(
                    fieldDto.getSelectOptions() != null
                        ? fieldDto.getSelectOptions().stream()
                            .map(opt -> new FormField.SelectOptionDto(opt.getLabel(), opt.getValue()))
                            .collect(Collectors.toList())
                        : null
                )
                .build();
    }

    private FormDto.FormFieldDto convertToFormFieldDto(FormField fieldEntity) {
        return FormDto.FormFieldDto.builder()
                .id(fieldEntity.getId())
                .name(fieldEntity.getName())
                .type(fieldEntity.getType())
                .selectOptions(
                    fieldEntity.getSelectOptions() != null
                        ? fieldEntity.getSelectOptions().stream()
                            .map(opt -> new FormDto.SelectOptionDto(opt.getLabel(), opt.getValue()))
                            .collect(Collectors.toList())
                        : null
                )
                .build();
    }
}
