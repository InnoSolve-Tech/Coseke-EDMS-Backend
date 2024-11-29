package com.edms.file_management.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        // Retrieve the current user ID (e.g., from the security context or session)
        // For now, return a mock value like 1L for testing
        return Optional.of(1L);
    }
}

