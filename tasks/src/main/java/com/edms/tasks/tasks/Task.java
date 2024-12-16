package com.edms.tasks.tasks;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private String title;
   private LocalDateTime date;
   private  String status;
   private String priority;
   private String description;
   private LocalDateTime startDate;
   private LocalDateTime dueDate;
   private String timelineReason;
   private String assignees;
   private String roles;
   private String deadline;


}
