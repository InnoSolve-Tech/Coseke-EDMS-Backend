package com.edms.workflows.WorkflowComments;

import com.edms.workflows.ActiveWorkflows.ActiveWorkflows;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowCommentsRepository extends JpaRepository<WorkflowComments, Long> {
    List<WorkflowComments> findByActiveWorkflows(ActiveWorkflows activeWorkflows);
}
