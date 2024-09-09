package com.cosek.edms.ActiveWorkflows;

import com.cosek.edms.WorkflowComments.WorkflowComments;
import com.cosek.edms.filemanager.FileManager;
import com.cosek.edms.helper.JsonMapConverter;
import com.cosek.edms.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class ActiveWorkflows {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stage;
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "nvarchar(max)")
    private Map<String, Object> metadata;
    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_manager_id", referencedColumnName = "id")
    private FileManager fileManager;
    @OneToMany(cascade = CascadeType.DETACH)
    @Column(name = "activeWorkflows")
    private Collection<WorkflowComments> workflowComments;
}
