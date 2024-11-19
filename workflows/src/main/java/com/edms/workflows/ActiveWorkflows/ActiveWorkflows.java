package com.edms.workflows.ActiveWorkflows;

import com.edms.workflows.WorkflowComments.WorkflowComments;
import com.edms.workflows.Workflows.Workflows;
import com.edms.workflows.helper.JsonMapConverter;
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
    @OneToMany(cascade = CascadeType.DETACH)
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "workflows_id", referencedColumnName = "id")
    private Workflows workflows;
    @OneToMany(cascade = CascadeType.DETACH)
    @Column(name = "activeWorkflows")
    private Collection<WorkflowComments> workflowComments;
}
