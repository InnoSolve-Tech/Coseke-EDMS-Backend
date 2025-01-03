package com.edms.forms.Form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.edms.forms.FormField.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormDto {
    private Long id;
    private String name;
    private String description;
    private List<FormFieldDto> formFields;
    private SelectOptionsDto selectOptions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FormFieldDto {
        private Long id;
        private String name;
        private FieldType type;
        private List<SelectOptionDto> selectOptions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectOptionDto {
        private String label;
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectOptionsDto {
        private List<String> options;
    }
}
