package com.edms.tasks.tasks;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }


    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(taskDetails.getTitle());
                    task.setDate(taskDetails.getDate());
                    task.setDeadline(taskDetails.getDeadline());
                    task.setStatus(taskDetails.getStatus());
                    task.setDescription(taskDetails.getDescription());
                    task.setPriority(taskDetails.getPriority());
                    task.setAssignees(taskDetails.getAssignees());
                    task.setDueDate(taskDetails.getDueDate());
                    task.setRoles(taskDetails.getRoles());
                    return taskRepository.save(task);
                }).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getAllTask() {
        return taskRepository.findAll();
    }
}

