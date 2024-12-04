package com.edms.workflows.node;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Position {
    private Double x;
    private Double y;
} 