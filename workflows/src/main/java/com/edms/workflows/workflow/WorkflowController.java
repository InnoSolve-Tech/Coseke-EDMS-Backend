package com.edms.workflows.workflow;

import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestBody Workflow workflow) {
        return ResponseEntity.ok(workflowService.createWorkflow(workflow));
    }

    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflow(id));
    }

    @PutMapping("/edit")
    public ResponseEntity<Workflow> updateWorkflow(@RequestBody Workflow updatedWorkflow) {
        return ResponseEntity.ok(workflowService.updateWorkflow(updatedWorkflow.getId(), updatedWorkflow));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, String>> deleteWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.deleteWorkflow(id));
    }
} 