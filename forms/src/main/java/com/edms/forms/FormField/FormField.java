package com.edms.forms.FormField;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.edms.forms.FormCreation.FormCreation;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form_fields")
public class FormField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false)
    private String type;

    @ElementCollection
    @CollectionTable(name = "field_select_options", joinColumns = @JoinColumn(name = "field_id"))
    @Column(name = "option_value")
    private List<String> selectOptions;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    @JsonIgnore
    private FormCreation form;
}
