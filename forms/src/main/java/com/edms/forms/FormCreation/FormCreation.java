package com.edms.forms.FormCreation;

import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form_creation")
public class FormCreation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(name = "form_fields", joinColumns = @JoinColumn(name = "form_fields_id"))
    @MapKeyColumn(name = "field_name")
    @Column(name = "field_type")
    private Map<String, String> fieldDefinitions = new HashMap<>();
}