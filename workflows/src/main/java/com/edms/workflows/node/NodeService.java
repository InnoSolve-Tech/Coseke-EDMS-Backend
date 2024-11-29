package com.edms.workflows.node;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NodeService {
    private final NodeRepository nodeRepository;
    
    public Node saveNode(Node node) {
        return nodeRepository.save(node);
    }
} 