package com.cosek.edms.WorkflowComments;

import com.cosek.edms.ActiveWorkflows.ActiveWorkflows;
import com.cosek.edms.Workflows.Workflows;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workflow-comments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WorkflowCommentsController {

    private final WorkflowCommentsService workflowCommentsService;

    // Get all workflow comments
    @GetMapping
    public ResponseEntity<List<WorkflowComments>> getAllWorkflowComments() {
        List<WorkflowComments> comments = workflowCommentsService.getAllWorkflowComments();
        return ResponseEntity.ok(comments);
    }

    // Get a workflow comment by ID
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowComments> getWorkflowCommentById(@PathVariable Long id) {
        Optional<WorkflowComments> comment = workflowCommentsService.getWorkflowCommentById(id);
        return comment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new workflow comment
    @PostMapping
    public ResponseEntity<WorkflowComments> createWorkflowComment(@RequestBody WorkflowComments workflowComment) {
        WorkflowComments createdComment = workflowCommentsService.createWorkflowComment(workflowComment);
        return ResponseEntity.ok(createdComment);
    }

    // Update an existing workflow comment
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowComments> updateWorkflowComment(@PathVariable Long id, @RequestBody WorkflowComments updatedComment) {
        WorkflowComments updatedWorkflowComment = workflowCommentsService.updateWorkflowComment(id, updatedComment);
        return ResponseEntity.ok(updatedWorkflowComment);
    }

    // Delete a workflow comment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflowComment(@PathVariable Long id) {
        workflowCommentsService.deleteWorkflowComment(id);
        return ResponseEntity.noContent().build();
    }

    // Get all comments for a specific workflow
    @GetMapping("/by-workflow/{workflowId}")
    public ResponseEntity<List<WorkflowComments>> getCommentsByWorkflow(@PathVariable Long workflowId) {
        ActiveWorkflows workflows = new ActiveWorkflows();  // Assume you get this from some service or repository
        workflows.setId(workflowId);
        List<WorkflowComments> comments = workflowCommentsService.findCommentsByWorkflow(workflows);
        return ResponseEntity.ok(comments);
    }
}
