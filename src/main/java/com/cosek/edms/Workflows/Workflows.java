package com.cosek.edms.Workflows;

import com.cosek.edms.WorkflowComments.WorkflowComments;
import com.cosek.edms.helper.JsonMapConverter;
import com.cosek.edms.user.User;
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
    private String name;
    private String documentType;
    private List<String> processPath;
}
