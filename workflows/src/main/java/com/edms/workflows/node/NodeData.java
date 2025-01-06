package com.edms.workflows.node;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Data;

@Embeddable
@Data
public class NodeData {
    private String label;
    private String description;
    private String nodeId;
    
    @Embedded
    private Assignee assignee;

    private String formId;
    
    private String dueDate;
} 