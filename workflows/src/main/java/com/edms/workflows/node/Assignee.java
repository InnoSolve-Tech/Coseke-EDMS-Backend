package com.edms.workflows.node;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Assignee {
    private String assignee_type; // "role" or "user"
    private String assignee_id;
} 