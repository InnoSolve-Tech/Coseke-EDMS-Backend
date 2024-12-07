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

        return workflowRepository.save(workflow);
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
            updatedWorkflow.getNodes().forEach(node -> node.setWorkflow(existingWorkflow));
            existingWorkflow.setNodes(updatedWorkflow.getNodes());
        }

        // Update edges
        if (updatedWorkflow.getEdges() != null) {
            updatedWorkflow.getEdges().forEach(edge -> edge.setWorkflow(existingWorkflow));
            existingWorkflow.setEdges(updatedWorkflow.getEdges());
        }

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
