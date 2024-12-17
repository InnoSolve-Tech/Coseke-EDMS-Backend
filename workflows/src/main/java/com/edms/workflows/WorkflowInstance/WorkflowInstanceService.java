package com.edms.workflows.WorkflowInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edms.workflows.workflow.Workflow;
import com.edms.workflows.workflow.WorkflowRepository;

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
        Workflow workflow;
            workflow = workflowRepository.findById(workflowInstance.getWorkflow().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Workflow with ID " + workflowInstance.getWorkflow().getId() + " not found"));
        workflowInstance.setWorkflow(workflow);
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
}
