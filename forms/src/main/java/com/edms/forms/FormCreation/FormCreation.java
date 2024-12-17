package com.edms.forms.FormCreation;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import com.edms.forms.FormField.FormField;

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

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormField> fieldDefinitions = new ArrayList<>();
}
