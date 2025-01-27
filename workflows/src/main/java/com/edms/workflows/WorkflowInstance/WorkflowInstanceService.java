package com.edms.workflows.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.edms.workflows.workflow.Workflow;
import com.edms.workflows.workflow.WorkflowRepository;
import com.edms.workflows.Condition.Condition;
import com.edms.workflows.helper.Microservice;
import com.edms.workflows.helper.OperatorPicker;
import com.edms.workflows.helper.OperatorPicker.Operator;
import com.edms.workflows.node.Node;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;

@Service
public class WorkflowInstanceService {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowRepository workflowRepository;

    @Autowired
    public WorkflowInstanceService(WorkflowInstanceRepository workflowInstanceRepository, WorkflowRepository workflowRepository) {
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowRepository = workflowRepository;
    }

    /**
     * Create or save a new WorkflowInstance
     *
     * @param workflowInstance the workflow instance to save
     * @return the saved workflow instance
     */
    public WorkflowInstance saveWorkflowInstance(WorkflowInstance workflowInstance) {
        if (workflowInstance.getWorkflow() == null || workflowInstance.getWorkflow().getId() == null) {
            throw new IllegalArgumentException("Workflow information is missing or incomplete");
        }
    
        Workflow workflow = workflowRepository.findById(workflowInstance.getWorkflow().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow with ID " + workflowInstance.getWorkflow().getId() + " not found"));
    
        workflowInstance.setWorkflow(workflow);
        workflowInstance.setStatus("Created");
    
        // Find the start node
        Optional<Node> startNode = workflow.getNodes().stream()
                .filter(node -> "start".equals(node.getType()))
                .findFirst();
    
        if (startNode.isEmpty()) {
            throw new IllegalArgumentException("No start node found in the workflow with ID " + workflow.getId());
        }
    
        workflowInstance.setCurrentStep(startNode.get().getId());
        return workflowInstanceRepository.save(workflowInstance);
    }

    public WorkflowInstance changeCurrentStep(Long id, String step) {
        WorkflowInstance instance = workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance with ID " + id + " not found"));
    
        List<Node> nodes = instance.getWorkflow().getNodes();
    
        // Handle "Completed" step
        if (step.equals("Completed")) {
            return updateInstanceStep(instance, step, "Completed");
        }
    
        // Validate node existence for non-"Completed" steps
        final String stepFinal = step;
        if (!nodes.stream().anyMatch(node -> node.getId().equals(stepFinal))) {
            throw new IllegalArgumentException("Node with ID " + step + " not found in the workflow");
        }

        Node currentNode = getCurrentNode(nodes, step);
        if (currentNode.getType().equals("decision")) {
            List<Node> formNodes = nodes.stream().filter(node -> node.getType().equals("form"))
                    .filter(node -> node.getData().getFormId().equals(currentNode.getData().getFormId())
                            && instance.getMetadata().get(node.getId()) != null)
                    .collect(Collectors.toList());
    
            if (formNodes.isEmpty()) {
                throw new IllegalStateException("No form nodes found.");
            }
    
            boolean allConditionsMet = evaluateConditions(instance, currentNode, formNodes);
            if (allConditionsMet) {
                step = currentNode.getData().getIfTrue();
                return updateInstanceStep(instance, step, getCurrentNode(nodes, currentNode.getData().getIfTrue()).getType());
            } else {
                step = currentNode.getData().getIfFalse();
                return updateInstanceStep(instance, step, getCurrentNode(nodes, currentNode.getData().getIfFalse()).getType());
            }
        }
    
        return updateInstanceStep(instance, step, getCurrentNode(nodes, step).getType());
    }
    
    private Node getCurrentNode(List<Node> nodes, String currentStep) {
        return nodes.stream()
                .filter(node -> node.getId().equals(currentStep))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Current step not found in the workflow"));
    }
    
    private boolean evaluateConditions(WorkflowInstance instance, Node currentNode, List<Node> formNodes) {
        // Use dependency injection for Microservice instead of creating a new instance here
        Microservice microservice = new Microservice();
    
        // Get the ID of the first form node
        String formNodeId = formNodes.get(0).getId();
        String url = microservice.getFormsRoute("/form-records/" + instance.getMetadata().get(formNodeId));
        System.out.println("Constructed URL: " + url);
    
        // Make GET request to retrieve the form data
        ResponseEntity<HashMap> response = microservice.get(url, HashMap.class);
    
        if (response.getBody() == null) {
            throw new IllegalStateException("Response body is null.");
        }
    
        // Retrieve formFieldValues
        List<HashMap> formFieldValues = (List<HashMap>) response.getBody().get("formFieldValues");
        if (formFieldValues == null) {
            throw new IllegalStateException("Form field Values are null.");
        }
    
        // Evaluate each condition in the current node
        for (Condition condition : currentNode.getData().getCondition()) {
            OperatorPicker operatorPicker = new OperatorPicker();
            Operator operator = operatorPicker.pickOperator(condition.getOperator());
    
            // Find the form field that matches the condition's field
            System.out.println("Form Field Values: " + formFieldValues);
            HashMap matchedField = formFieldValues.stream()
                .filter(formFieldMap -> {
                    HashMap formField = (HashMap) formFieldMap.get("formField");
                    return formField != null && condition.getField().equals(formField.get("name"));
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Form field with ID " + condition.getField() + " not found"));
    
            // Extract the value of the matched field
            String value = (String) matchedField.get("value");
    
            // Apply the operator to check the condition
            boolean conditionMet = operator.apply(value, condition.getValue());
    
            if (!conditionMet) {
                return false; // Exit early if any condition is not met
            }
        }
    
        // Return true if all conditions are met
        return true;
    }
    
    
    private WorkflowInstance updateInstanceStep(WorkflowInstance instance, String step, String status) {
        instance.setCurrentStep(step);
        instance.setStatus(status);
        return workflowInstanceRepository.save(instance);
    }
    
    /**
     * Get all WorkflowInstances
     *
     * @return list of workflow instances
     */
    public List<WorkflowInstance> getAllWorkflowInstances() {
        return workflowInstanceRepository.findAll();
    }

    /**
     * Get a WorkflowInstance by ID
     *
     * @param id the ID of the workflow instance
     * @return the workflow instance
     */
    public WorkflowInstance getWorkflowInstanceById(Long id) {
        return workflowInstanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance with ID " + id + " not found"));
    }

    /**
     * Delete a WorkflowInstance by ID
     *
     * @param id the ID of the workflow instance to delete
     */
    public void deleteWorkflowInstanceById(Long id) {
        if (!workflowInstanceRepository.existsById(id)) {
            throw new IllegalArgumentException("WorkflowInstance with ID " + id + " does not exist");
        }
        workflowInstanceRepository.deleteById(id);
    }

    /**
 * Update an existing WorkflowInstance by ID.
 *
 * @param id                 the ID of the workflow instance to update
 * @param updatedInstance    the updated workflow instance data
 * @return the updated workflow instance
 */
public WorkflowInstance updateWorkflowInstance(Long id, WorkflowInstance updatedInstance) {
    // Retrieve the existing instance
    WorkflowInstance existingInstance = workflowInstanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("WorkflowInstance with ID " + id + " not found"));

    // Update fields if present in the updatedInstance object
    if (updatedInstance.getName() != null) {
        existingInstance.setName(updatedInstance.getName());
    }
    if (updatedInstance.getStatus() != null) {
        existingInstance.setStatus(updatedInstance.getStatus());
    }
    if (updatedInstance.getCurrentStep() != null) {
        existingInstance.setCurrentStep(updatedInstance.getCurrentStep());
    }
    if (updatedInstance.getMetadata() != null) {
        existingInstance.setMetadata(updatedInstance.getMetadata());
    }
    if (updatedInstance.getWorkflow() != null && updatedInstance.getWorkflow().getId() != null) {
        Workflow workflow = workflowRepository.findById(updatedInstance.getWorkflow().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow with ID " + updatedInstance.getWorkflow().getId() + " not found"));
        existingInstance.setWorkflow(workflow);
    }

    // Save and return the updated instance
    return workflowInstanceRepository.save(existingInstance);
}

}
