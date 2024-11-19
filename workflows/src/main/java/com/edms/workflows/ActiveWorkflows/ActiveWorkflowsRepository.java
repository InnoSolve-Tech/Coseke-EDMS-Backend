package com.edms.workflows.ActiveWorkflows;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveWorkflowsRepository extends JpaRepository<ActiveWorkflows, Long> {
}
