package com.cosek.edms.ActiveWorkflows;

import com.cosek.edms.WorkflowComments.WorkflowComments;
import com.cosek.edms.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class ActiveWorkflows {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stage;
    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @OneToMany(cascade = CascadeType.DETACH)
    @Column(name = "activeWorkflows")
    private Collection<WorkflowComments> workflowComments;
}
