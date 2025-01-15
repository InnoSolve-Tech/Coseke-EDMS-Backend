package com.edms.workflows.Condition;

import ch.qos.logback.core.pattern.parser.Node;
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
    private Node node;
}
