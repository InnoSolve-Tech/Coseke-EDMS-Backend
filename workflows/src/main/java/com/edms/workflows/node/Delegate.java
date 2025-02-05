package com.edms.workflows.node;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Delegate {
    private String delegate_type; // "role" or "user"
    private String delegate_id;   
}