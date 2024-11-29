package com.edms.workflows.workflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    
    @Transactional
    public Workflow createWorkflow(Workflow workflow) {
        return workflowRepository.save(workflow);
    }
    
    public Workflow getWorkflow(Long id) {
        return workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));
    }
} 