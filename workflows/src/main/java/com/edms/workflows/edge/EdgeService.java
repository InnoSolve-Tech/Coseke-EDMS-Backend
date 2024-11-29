package com.edms.workflows.edge;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EdgeService {
    private final EdgeRepository edgeRepository;
    
    public Edge saveEdge(Edge edge) {
        return edgeRepository.save(edge);
    }
} 