package com.edms.workflows.WorkflowInstance;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;

import com.edms.workflows.workflow.Workflow;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "workflow_instances")
@Data
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false)
    private String status;

    @ElementCollection
    @CollectionTable(name = "workflow_metadata", joinColumns = @JoinColumn(name = "workflow_instance_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> metadata;
}