package com.cosek.edms.WorkflowComments;

import com.cosek.edms.Workflows.Workflows;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowCommentsService {

    private final WorkflowCommentsRepository workflowCommentsRepository;

    @Transactional
    public WorkflowComments createWorkflowComment(WorkflowComments workflowComment) {
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
