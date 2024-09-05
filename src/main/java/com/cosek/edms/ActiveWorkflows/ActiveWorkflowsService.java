package com.cosek.edms.ActiveWorkflows;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActiveWorkflowsService {

    private final ActiveWorkflowsRepository activeWorkflowsRepository;

    // Create or Update ActiveWorkflow
    public ActiveWorkflows saveOrUpdate(ActiveWorkflows activeWorkflow) {
        return activeWorkflowsRepository.save(activeWorkflow);
    }

    // Get ActiveWorkflow by ID
    public Optional<ActiveWorkflows> findById(Long id) {
        return activeWorkflowsRepository.findById(id);
    }

    // Get all ActiveWorkflows
    public List<ActiveWorkflows> findAll() {
        return activeWorkflowsRepository.findAll();
    }

    // Delete ActiveWorkflow by ID
    public void deleteById(Long id) {
        activeWorkflowsRepository.deleteById(id);
    }

    // Change the stage of an ActiveWorkflow
    @Transactional
    public ActiveWorkflows changeStage(Long id, String newStage) {
        Optional<ActiveWorkflows> activeWorkflowOpt = activeWorkflowsRepository.findById(id);

        if (activeWorkflowOpt.isPresent()) {
            ActiveWorkflows activeWorkflow = activeWorkflowOpt.get();
            activeWorkflow.setStage(newStage);
            return activeWorkflowsRepository.save(activeWorkflow);
        } else {
            throw new RuntimeException("ActiveWorkflow not found with id: " + id);
        }
    }
}
