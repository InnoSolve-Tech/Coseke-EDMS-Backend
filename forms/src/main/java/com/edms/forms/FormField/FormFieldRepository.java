package com.edms.forms.FormField;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, Long> {  
} 