package com.edms.forms.FormRecords;

import jakarta.persistence.*;
import lombok.*;
import com.edms.forms.FormCreation.FormCreation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form_records")
public class FormRecords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_creation_id", nullable = false)
    private FormCreation formCreation;

    @ElementCollection
    @CollectionTable(name = "form_record_values", joinColumns = @JoinColumn(name = "form_record_id"))
    @MapKeyColumn(name = "field_name")
    @Column(name = "field_value")
    private Map<String, String> submissionValues = new HashMap<>();

    private LocalDateTime submissionDate;

    @PrePersist
    public void prePersist() {
        this.submissionDate = LocalDateTime.now();
    }
}
