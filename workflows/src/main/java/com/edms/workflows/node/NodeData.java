package com.edms.workflows.node;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

import com.edms.workflows.Condition.Condition;
import com.edms.workflows.Notification.Notification;

@Embeddable
@Data
public class NodeData {
    private String label;
    private String description;
    private String nodeId;
    
    @Embedded
    private Assignee assignee;

    @Embedded
    private Delegate delegate;

    private String formId;

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Condition> condition;

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notification;

    private String ifFalse;

    private String ifTrue;
    
    private String dueDate;

    private Boolean shouldDelegate;
} 