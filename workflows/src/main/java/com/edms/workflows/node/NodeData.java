package com.edms.workflows.node;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class NodeData {
    private String label;
    private String description;
    private String nodeId;
} 