package com.cosek.edms.Workflows;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowsService {

    @Autowired
    private WorkflowsRepository workflowsRepository;

    public List<Workflows> getAllWorkflows() {
        return workflowsRepository.findAll();
    }

    public Workflows getWorkflowById(Long id) {
        return workflowsRepository.findById(id).orElseThrow();
    }

    public Workflows createWorkflow(Workflows workflow) {
        return workflowsRepository.save(workflow);
    }

    public HashMap<String, String> deleteWorkflow(Long id) {
        HashMap<String, String> response = new HashMap<>();
        Optional<Workflows> existingWorkflowOpt = workflowsRepository.findById(id);
        if (existingWorkflowOpt.isPresent()) {
            workflowsRepository.deleteById(id);
            response.put("message", "Workflow deleted successfully");
            response.put("status", "success");
        } else {
            response.put("message", "Workflow not found with id: " + id);
            response.put("status", "error");
        }
        return response;
    }

    @Transactional
    public Workflows modifyWorkflow(Workflows newWorkflow) {
        Optional<Workflows> existingWorkflowOpt = workflowsRepository.findById(newWorkflow.getId());
        if (existingWorkflowOpt.isPresent()) {
            Workflows existingWorkflow = existingWorkflowOpt.get();
            existingWorkflow.setName(newWorkflow.getName());
            existingWorkflow.setDocumentType(newWorkflow.getDocumentType());
            return workflowsRepository.save(existingWorkflow);
        } else {
            throw new RuntimeException("Workflow not found with id: " + newWorkflow.getId());
        }
    }
}
