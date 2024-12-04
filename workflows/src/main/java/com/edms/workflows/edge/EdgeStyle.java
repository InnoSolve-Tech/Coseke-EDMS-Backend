package com.edms.workflows.edge;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class EdgeStyle {
    private Integer strokeWidth;
} 