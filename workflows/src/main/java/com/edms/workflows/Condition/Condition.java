package com.edms.workflows.Condition;

import com.edms.workflows.node.Node;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "condition")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
    @Id
    private String id;

    @Column(nullable = false)
    private String field;

    @Column(nullable = false)
    private String operator;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "node_id")
    @JsonIgnore
    private Node node;
}
