package com.edms.workflows.Notification;

import com.edms.workflows.node.Node;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "notification")
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String subject;
    private String body;
    @ManyToOne
    @JoinColumn(name = "node_id")
    @JsonIgnore
    private Node node;
}

