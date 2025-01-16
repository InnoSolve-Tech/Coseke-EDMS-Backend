package com.edms.workflows.node;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

import com.edms.workflows.Condition.Condition;

@Embeddable
@Data
public class NodeData {
    private String label;
    private String description;
    private String nodeId;
    
    @Embedded
    private Assignee assignee;

    private String formId;

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Condition> condition;

    private String ifFalse;

    private String ifTrue;
    
    private String dueDate;
} 