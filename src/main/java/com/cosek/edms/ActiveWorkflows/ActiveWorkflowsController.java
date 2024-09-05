package com.cosek.edms.ActiveWorkflows;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/active-workflows")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ActiveWorkflowsController {

    private final ActiveWorkflowsService activeWorkflowsService;

    // Create or Update ActiveWorkflow
    @PostMapping
    public ResponseEntity<ActiveWorkflows> createOrUpdate(@RequestBody ActiveWorkflows activeWorkflows) {
        ActiveWorkflows savedWorkflow = activeWorkflowsService.saveOrUpdate(activeWorkflows);
        return new ResponseEntity<>(savedWorkflow, HttpStatus.CREATED);
    }

    // Get ActiveWorkflow by ID
    @GetMapping("/{id}")
    public ResponseEntity<ActiveWorkflows> getById(@PathVariable Long id) {
        Optional<ActiveWorkflows> workflow = activeWorkflowsService.findById(id);
        return workflow.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get all ActiveWorkflows
    @GetMapping
    public ResponseEntity<List<ActiveWorkflows>> getAll() {
        List<ActiveWorkflows> workflows = activeWorkflowsService.findAll();
        return new ResponseEntity<>(workflows, HttpStatus.OK);
    }

    // Delete ActiveWorkflow by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        activeWorkflowsService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Change the stage of an ActiveWorkflow
    @PatchMapping("/{id}/stage")
    public ResponseEntity<ActiveWorkflows> changeStage(@PathVariable Long id, @RequestParam String newStage) {
        try {
            ActiveWorkflows updatedWorkflow = activeWorkflowsService.changeStage(id, newStage);
            return new ResponseEntity<>(updatedWorkflow, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
