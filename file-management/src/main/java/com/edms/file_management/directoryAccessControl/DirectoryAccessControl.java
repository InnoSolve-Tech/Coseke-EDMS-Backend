package com.edms.file_management.directoryAccessControl;

import com.edms.file_management.directory.Directory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectoryAccessControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private AccessType accessType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directory_id", insertable = true, updatable = false)
    private Directory directory;

    private List<Integer> roles;
}
