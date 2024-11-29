package com.edms.workflows.edge;

import com.edms.workflows.workflow.Workflow;
import com.edms.workflows.edge.EdgeStyle;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Edge {
    @Id
    private String id;
    
    private String type;
    private Boolean animated;
    private String source;
    private String sourceHandle;
    private String target;
    private String targetHandle;
    
    @Embedded
    private EdgeStyle style;
    
    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;
} 