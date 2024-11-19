package com.cosek.edms.WorkflowComments;

import com.cosek.edms.ActiveWorkflows.ActiveWorkflows;
import com.cosek.edms.Workflows.Workflows;
import com.cosek.edms.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class WorkflowComments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "active_workflow_id", referencedColumnName = "id")
    private ActiveWorkflows activeWorkflows;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
