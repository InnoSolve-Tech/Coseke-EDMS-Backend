package com.cosek.edms.search;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Search {
        @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

    private Long folderid;
    private String filename;
    private String document_type;
    private String document_name;
    private String last_modified_date;
    private String file_link;
    
}
