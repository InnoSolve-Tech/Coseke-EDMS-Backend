package com.edms.forms.FormCreation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormCreationRepository extends JpaRepository<FormCreation, Long> {
    Optional<FormCreation> findByName(String name);
}
