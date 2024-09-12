package com.cosek.edms.ActiveWorkflows;

import com.cosek.edms.Workflows.Workflows;
import com.cosek.edms.Workflows.WorkflowsRepository;
import com.cosek.edms.filemanager.FileManager;
import com.cosek.edms.filemanager.FileManagerRepository;
import com.cosek.edms.user.User;
import com.cosek.edms.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActiveWorkflowsService {

    private final ActiveWorkflowsRepository activeWorkflowsRepository;
    private final WorkflowsRepository workflowsRepository;
    private final FileManagerRepository fileManagerRepository;
    private final UserRepository userRepository;

    // Create or Update ActiveWorkflow
    public ActiveWorkflows saveOrUpdate(ActiveWorkflows activeWorkflow) {
        FileManager file = fileManagerRepository.findById(activeWorkflow.getFileManager().getId()).orElseThrow();
        Workflows workflows = workflowsRepository.findById(activeWorkflow.getWorkflows().getId()).orElseThrow();

        // Extract the authenticated user from the SecurityContext
        String username = getAuthenticatedEmail();

        // Fetch the user by username (or adjust if you store user differently)
        User user = userRepository.findByEmail(username).orElseThrow();
        activeWorkflow.setUser(user);
        activeWorkflow.setFileManager(file);
        activeWorkflow.setWorkflows(workflows);
        return activeWorkflowsRepository.save(activeWorkflow);
    }

    private String getAuthenticatedEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // Get ActiveWorkflow by ID
    public Optional<ActiveWorkflows> findById(Long id) {
        return activeWorkflowsRepository.findById(id);
    }

    public List<ActiveWorkflows> findByUserId() {
        // Extract the authenticated user from the SecurityContext
        String username = getAuthenticatedEmail();

        // Fetch the user by username (or adjust if you store user differently)
        User user = userRepository.findByEmail(username).orElseThrow();
        return activeWorkflowsRepository.findByUserId(user.getId());
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
    public ActiveWorkflows changeStage(Long id) {
        Optional<ActiveWorkflows> activeWorkflowOpt = activeWorkflowsRepository.findById(id);
        if (activeWorkflowOpt.isEmpty()) {
            throw new RuntimeException("ActiveWorkflow not found with id: " + id);
        }

        ActiveWorkflows activeWorkflow = activeWorkflowOpt.get();
        System.out.println("Found workflow: " + activeWorkflow);
        List<String> processPath = activeWorkflow.getWorkflows().getProcessPath();
        int currentIndex = processPath.indexOf(activeWorkflow.getStage());

        System.out.println("Current stage: " + activeWorkflow.getStage() + " at index " + currentIndex);

        if (currentIndex == processPath.size() - 1) {
            activeWorkflow.setStage("Completed");
        } else if (currentIndex != -1) {
            activeWorkflow.setStage(processPath.get(currentIndex + 1));
        } else {
            throw new RuntimeException("Current stage not found in process path");
        }

        return activeWorkflowsRepository.save(activeWorkflow);
    }


}
