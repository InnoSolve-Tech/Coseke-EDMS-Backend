package com.cosek.edms.ActiveWorkflows;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveWorkflowsRepository extends JpaRepository<ActiveWorkflows, Long> {
    List<ActiveWorkflows> findByUserId(Long userId); // Custom method to find by userId
}
