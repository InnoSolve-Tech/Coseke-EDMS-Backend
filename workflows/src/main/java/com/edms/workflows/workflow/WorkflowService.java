package com.edms.workflows.workflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    
    @Transactional
    public Workflow createWorkflow(Workflow workflow) {
        // Ensure nodes are attached to the workflow
        if (workflow.getNodes() != null) {
            workflow.getNodes().forEach(node -> node.setWorkflow(workflow));
        }
        
        // Ensure edges are attached to the workflow
        if (workflow.getEdges() != null) {
            workflow.getEdges().forEach(edge -> edge.setWorkflow(workflow));
        }
        
        return workflowRepository.save(workflow);
    }
    
    public Workflow getWorkflow(Long id) {
        return workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));
    }
} 