package com.cosek.edms.ActiveWorkflows;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveWorkflowsRepository extends JpaRepository<ActiveWorkflows, Long> {
}
