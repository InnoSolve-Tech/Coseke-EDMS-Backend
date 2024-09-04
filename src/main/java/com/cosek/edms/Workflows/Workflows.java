package com.cosek.edms.Workflows;

import com.cosek.edms.WorkflowComments.WorkflowComments;
import com.cosek.edms.helper.JsonMapConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class Workflows {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long name;
    private String documentType;
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "nvarchar(max)")
    private Map<String, Object> metadata;
    private List<String> processPath;
    @OneToMany(cascade = CascadeType.DETACH)
    @Column(name = "workflows")
    private Collection<WorkflowComments> workflowComments;

}
