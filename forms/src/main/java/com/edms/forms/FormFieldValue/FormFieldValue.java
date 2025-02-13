package com.edms.forms.FormFieldValue;

import com.edms.forms.FormField.FormField;
import com.edms.forms.FormRecord.FormRecord;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "form_field_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormFieldValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_field_id", nullable = false)
    private FormField formField;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "form_record_id")
    @JsonIgnore
    private FormRecord formRecord;
}
