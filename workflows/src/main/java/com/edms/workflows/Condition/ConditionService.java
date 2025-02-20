package com.edms.workflows.Condition;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ConditionService {
    private final ConditionRepository conditionRepository;
    
    public Condition createCondition(Condition condition) {
        return conditionRepository.save(condition);
    }
    
    public Condition findById(String id) {
        return conditionRepository.findById(id).orElse(null);
    }
    
    public void delete(String id) {
        conditionRepository.deleteById(id);
    }

    @Transactional
    public Condition updateCondition(Condition condition) {
        return conditionRepository.save(condition);
    }

    public List<Condition> findAll() {
        return conditionRepository.findAll();
    }
    
}
