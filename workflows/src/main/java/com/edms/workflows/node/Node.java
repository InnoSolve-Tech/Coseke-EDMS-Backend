package com.edms.workflows.node;

import com.edms.workflows.workflow.Workflow;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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