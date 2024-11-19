package com.cosek.edms.Workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/workflows")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WorkflowsController {

    private final WorkflowsService workflowsService;

    // Get all workflows
    @GetMapping
    public ResponseEntity<List<Workflows>> getAllWorkflows() {
        List<Workflows> workflows = workflowsService.getAllWorkflows();
        return ResponseEntity.ok(workflows);
    }

    // Get a workflow by ID
    @GetMapping("/{id}")
    public ResponseEntity<Workflows> getWorkflowById(@PathVariable Long id) {
        Workflows workflow = workflowsService.getWorkflowById(id);
        return ResponseEntity.ok(workflow);
    }

    // Create a new workflow
    @PostMapping
    public ResponseEntity<Workflows> createWorkflow(@RequestBody Workflows workflow) {
        Workflows createdWorkflow = workflowsService.createWorkflow(workflow);
        return ResponseEntity.ok(createdWorkflow);
    }

    // Update an existing workflow
    @PutMapping("/{id}")
    public ResponseEntity<Workflows> modifyWorkflow(@PathVariable Long id, @RequestBody Workflows newWorkflow) {
        newWorkflow.setId(id);  // Ensure the ID is set in the new workflow object
        Workflows updatedWorkflow = workflowsService.modifyWorkflow(newWorkflow);
        return ResponseEntity.ok(updatedWorkflow);
    }

    // Delete a workflow
    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, String>> deleteWorkflow(@PathVariable Long id) {
        HashMap<String, String> response = workflowsService.deleteWorkflow(id);
        return ResponseEntity.ok(response);
    }
}
