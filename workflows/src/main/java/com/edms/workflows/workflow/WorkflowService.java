package com.edms.workflows.workflow;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

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

        Workflow newWorkflow = workflowRepository.save(workflow);

        newWorkflow.getNodes().forEach(node -> {
            if (node.getType() == "decision") {
                node.getData().getCondition().forEach(condition -> condition.setNode(node));
            }
        });
        return newWorkflow;
    }

    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    public Workflow getWorkflow(Long id) {
        return workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));
    }

    @Transactional
    public Workflow updateWorkflow(Long id, Workflow updatedWorkflow) {
        // Fetch the existing workflow
        Workflow existingWorkflow = workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));
    
        // Update properties
        existingWorkflow.setName(updatedWorkflow.getName());
        existingWorkflow.setDescription(updatedWorkflow.getDescription());
    
        // Update nodes
        if (updatedWorkflow.getNodes() != null) {
            // Remove nodes that are no longer in the updated list
            existingWorkflow.getNodes().removeIf(node -> !updatedWorkflow.getNodes().contains(node));
            
            // Add new nodes from updatedWorkflow to existingWorkflow
            updatedWorkflow.getNodes().forEach(node -> {
                if (!existingWorkflow.getNodes().contains(node)) {
                    existingWorkflow.getNodes().add(node);
                    node.setWorkflow(existingWorkflow);  // Make sure the bidirectional relationship is maintained
                }
            });
        }
    
        // Update edges
        if (updatedWorkflow.getEdges() != null) {
            // Remove edges that are no longer in the updated list
            existingWorkflow.getEdges().removeIf(edge -> !updatedWorkflow.getEdges().contains(edge));
            
            // Add new edges from updatedWorkflow to existingWorkflow
            updatedWorkflow.getEdges().forEach(edge -> {
                if (!existingWorkflow.getEdges().contains(edge)) {
                    existingWorkflow.getEdges().add(edge);
                    edge.setWorkflow(existingWorkflow);  // Make sure the bidirectional relationship is maintained
                }
            });
        }

        updatedWorkflow.getNodes().forEach(node -> {
            if (node.getType() != "decision") {
                node.getData().getCondition().forEach(condition -> condition.setNode(node));
            }
        });
    
        // Save and return the updated workflow
        return workflowRepository.save(existingWorkflow);
    }
    
    @Transactional
    public HashMap<String,String> deleteWorkflow(Long id) {
        workflowRepository.deleteById(id);
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "Workflow deleted successfully");
        return response;
    }
}
