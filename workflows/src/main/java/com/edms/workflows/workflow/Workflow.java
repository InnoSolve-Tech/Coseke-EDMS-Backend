package com.edms.workflows.workflow;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.edms.workflows.node.Node;
import com.edms.workflows.edge.Edge;

@Entity
@Data
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "workflow")
    private List<Node> nodes;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "workflow")
    private List<Edge> edges;
} 