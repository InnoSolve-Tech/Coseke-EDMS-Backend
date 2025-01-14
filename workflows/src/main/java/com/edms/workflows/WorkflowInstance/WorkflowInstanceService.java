package com.edms.workflows.WorkflowInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edms.workflows.workflow.Workflow;
import com.edms.workflows.workflow.WorkflowRepository;
import com.edms.workflows.node.Node;

import java.util.Optional;
import java.util.List;

@Service
public class WorkflowInstanceService {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowRepository workflowRepository;

    @Autowired
    public WorkflowInstanceService(WorkflowInstanceRepository workflowInstanceRepository, WorkflowRepository workflowRepository) {
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowRepository = workflowRepository;
    }

    /**
     * Create or save a new WorkflowInstance
     *
     * @param workflowInstance the workflow instance to save
     * @return the saved workflow instance
     */
    public WorkflowInstance saveWorkflowInstance(WorkflowInstance workflowInstance) {
        if (workflowInstance.getWorkflow() == null || workflowInstance.getWorkflow().getId() == null) {
            throw new IllegalArgumentException("Workflow information is missing or incomplete");
        }
    
        Workflow workflow = workflowRepository.findById(workflowInstance.getWorkflow().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow with ID " + workflowInstance.getWorkflow().getId() + " not found"));
    
        workflowInstance.setWorkflow(workflow);
        workflowInstance.setStatus("Created");
    
        // Find the start node
        Optional<Node> startNode = workflow.getNodes().stream()
                .filter(node -> "start".equals(node.getType()))
                .findFirst();
    
        if (startNode.isEmpty()) {
            throw new IllegalArgumentException("No start node found in the workflow with ID " + workflow.getId());
        }
    
        workflowInstance.setCurrentStep(startNode.get().getId());
        return workflowInstanceRepository.save(workflowInstance);
    }
    
    /**
     * Get all WorkflowInstances
     *
     * @return list of workflow instances
     */
    public List<WorkflowInstance> getAllWorkflowInstances() {
        return workflowInstanceRepository.findAll();
    }

    /**
     * Get a WorkflowInstance by ID
     *
     * @param id the ID of the workflow instance
     * @return the workflow instance
     */
    public WorkflowInstance getWorkflowInstanceById(Long id) {
        return workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance with ID " + id + " not found"));
    }

    /**
     * Delete a WorkflowInstance by ID
     *
     * @param id the ID of the workflow instance to delete
     */
    public void deleteWorkflowInstanceById(Long id) {
        if (!workflowInstanceRepository.existsById(id)) {
            throw new IllegalArgumentException("WorkflowInstance with ID " + id + " does not exist");
        }
        workflowInstanceRepository.deleteById(id);
    }

    /**
 * Update an existing WorkflowInstance by ID.
 *
 * @param id                 the ID of the workflow instance to update
 * @param updatedInstance    the updated workflow instance data
 * @return the updated workflow instance
 */
public WorkflowInstance updateWorkflowInstance(Long id, WorkflowInstance updatedInstance) {
    // Retrieve the existing instance
    WorkflowInstance existingInstance = workflowInstanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance with ID " + id + " not found"));

    // Update fields if present in the updatedInstance object
    if (updatedInstance.getName() != null) {
        existingInstance.setName(updatedInstance.getName());
    }
    if (updatedInstance.getStatus() != null) {
        existingInstance.setStatus(updatedInstance.getStatus());
    }
    if (updatedInstance.getCurrentStep() != null) {
        existingInstance.setCurrentStep(updatedInstance.getCurrentStep());
    }
    if (updatedInstance.getMetadata() != null) {
        existingInstance.setMetadata(updatedInstance.getMetadata());
    }
    if (updatedInstance.getWorkflow() != null && updatedInstance.getWorkflow().getId() != null) {
        Workflow workflow = workflowRepository.findById(updatedInstance.getWorkflow().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow with ID " + updatedInstance.getWorkflow().getId() + " not found"));
        existingInstance.setWorkflow(workflow);
    }

    // Save and return the updated instance
    return workflowInstanceRepository.save(existingInstance);
}

}
