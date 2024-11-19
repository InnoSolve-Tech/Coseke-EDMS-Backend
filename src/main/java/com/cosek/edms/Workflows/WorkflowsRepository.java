package com.cosek.edms.Workflows;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowsRepository extends JpaRepository<Workflows, Long> {
}
