package com.edms.forms.FormRecord;

import java.time.LocalDateTime;
import java.util.List;

import com.edms.forms.Form.Form;
import com.edms.forms.FormFieldValue.FormFieldValue;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "form_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "form_record_fields",
        joinColumns = @JoinColumn(name = "form_record_id"),
        inverseJoinColumns = @JoinColumn(name = "form_field_id")
    )
    private List<FormFieldValue> formFieldValues;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    
    @Column(nullable = false)
    private Long userId;

}
