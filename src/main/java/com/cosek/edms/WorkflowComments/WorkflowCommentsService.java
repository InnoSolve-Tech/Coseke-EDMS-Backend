package com.cosek.edms.WorkflowComments;

import com.cosek.edms.Workflows.Workflows;
import com.cosek.edms.Workflows.WorkflowsRepository;
import com.cosek.edms.user.User;
import com.cosek.edms.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowCommentsService {

    private final WorkflowCommentsRepository workflowCommentsRepository;

    private final WorkflowsRepository workflowsRepository;

    private final UserRepository userRepository;

    public WorkflowComments createWorkflowComment(WorkflowComments workflowComment) {
        // Find workflow by ID
        Workflows workflows = workflowsRepository.findById(workflowComment.getWorkflows().getId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + workflowComment.getWorkflows().getId()));

        User user = userRepository.findById(workflowComment.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + workflowComment.getUser().getId()));

        workflowComment.setWorkflows(workflows);

        workflowComment.setUser(user);

        return workflowCommentsRepository.save(workflowComment);
    }

    public Optional<WorkflowComments> getWorkflowCommentById(Long id) {
        return workflowCommentsRepository.findById(id);
    }

    public List<WorkflowComments> getAllWorkflowComments() {
        return workflowCommentsRepository.findAll();
    }

    @Transactional
    public WorkflowComments updateWorkflowComment(Long id, WorkflowComments updatedWorkflowComment) {
        return workflowCommentsRepository.findById(id)
                .map(existingComment -> {
                    existingComment.setMessage(updatedWorkflowComment.getMessage());
                    existingComment.setWorkflows(updatedWorkflowComment.getWorkflows());
                    existingComment.setUser(updatedWorkflowComment.getUser());
                    return workflowCommentsRepository.save(existingComment);
                })
                .orElseThrow(() -> new RuntimeException("WorkflowComment not found with id: " + id));
    }

    @Transactional
    public void deleteWorkflowComment(Long id) {
        workflowCommentsRepository.deleteById(id);
    }

    public List<WorkflowComments> findCommentsByWorkflow(Workflows workflows) {
        return workflowCommentsRepository.findByWorkflows(workflows);
    }
}
