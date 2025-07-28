package com.edms.file_management.fileAccessControl;

import com.edms.file_management.filemanager.FileManager;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FileAccessControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private AccessType accessType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_manager_id", insertable = true, updatable = true)
    @JsonBackReference
    private FileManager fileManager;

    private List<Integer> roles;
}
