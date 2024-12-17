package com.edms.workflows.WorkflowInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow-instances")
public class WorkflowInstanceController {

    private final WorkflowInstanceService workflowInstanceService;

    @Autowired
    public WorkflowInstanceController(WorkflowInstanceService workflowInstanceService) {
        this.workflowInstanceService = workflowInstanceService;
    }

    /**
     * Create a new WorkflowInstance
     *
     * @param workflowInstance the workflow instance to create
     * @return the created workflow instance
     */
    @PostMapping
    public ResponseEntity<WorkflowInstance> createWorkflowInstance(@RequestBody WorkflowInstance workflowInstance) {
        WorkflowInstance createdInstance = workflowInstanceService.saveWorkflowInstance(workflowInstance);
        return ResponseEntity.ok(createdInstance);
    }

    /**
     * Get all WorkflowInstances
     *
     * @return list of all workflow instances
     */
    @GetMapping
    public ResponseEntity<List<WorkflowInstance>> getAllWorkflowInstances() {
        return ResponseEntity.ok(workflowInstanceService.getAllWorkflowInstances());
    }

    /**
     * Get a WorkflowInstance by ID
     *
     * @param id the ID of the workflow instance
     * @return the workflow instance
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowInstance> getWorkflowInstanceById(@PathVariable Long id) {
        WorkflowInstance instance = workflowInstanceService.getWorkflowInstanceById(id);
        return ResponseEntity.ok(instance);
    }

    /**
     * Delete a WorkflowInstance by ID
     *
     * @param id the ID of the workflow instance to delete
     * @return a response indicating the deletion status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWorkflowInstance(@PathVariable Long id) {
        workflowInstanceService.deleteWorkflowInstanceById(id);
        return ResponseEntity.ok("WorkflowInstance with ID " + id + " has been deleted");
    }
}

