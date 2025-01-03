package com.edms.forms.FormFieldValue;

import com.edms.forms.FormField.FormField;

import jakarta.persistence.*;
import lombok.*;

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
}
