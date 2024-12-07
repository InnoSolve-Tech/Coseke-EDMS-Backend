package com.edms.workflows.node;

import com.edms.workflows.workflow.Workflow;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "node")
public class Node {
    @Id
    private String id;
    
    private String type;
    
    @Embedded
    private Position position;
    
    @Embedded
    private NodeData data;
    
    private Integer width;
    private Integer height;
    private Boolean selected;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "x", column = @Column(name = "position_absolute_x")),
        @AttributeOverride(name = "y", column = @Column(name = "position_absolute_y"))
    })
    private Position positionAbsolute;
    
    private Boolean dragging;
    
    @ManyToOne
    @JoinColumn(name = "workflow_id")
    @JsonIgnore
    private Workflow workflow;
} 