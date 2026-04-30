package com.teamtask.controller;

import com.teamtask.dto.TaskDtos.TaskRequest;
import com.teamtask.dto.TaskDtos.TaskResponse;
import com.teamtask.dto.TaskDtos.TaskStatusRequest;
import com.teamtask.model.Role;
import com.teamtask.model.Task;
import com.teamtask.model.User;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.TaskRepository;
import com.teamtask.repository.UserRepository;
import com.teamtask.service.CurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public TaskController(
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @Transactional
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setAssignedTo(userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
        task.setProject(projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found")));
        return toResponse(taskRepository.save(task));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<TaskResponse> all() {
        User user = currentUserService.get();
        List<Task> tasks = user.getRole() == Role.ADMIN
                ? taskRepository.findAll()
                : taskRepository.findByAssignedToEmail(user.getEmail());
        return tasks.stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}")
    @Transactional
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        User user = currentUserService.get();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (user.getRole() != Role.ADMIN && !task.getAssignedTo().getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("You can only update your assigned tasks");
        }
        task.setStatus(request.status());
        return toResponse(taskRepository.save(task));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskRepository.deleteById(id);
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getAssignedTo().getId(),
                task.getAssignedTo().getName(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getDeadline(),
                task.getCreatedAt()
        );
    }
}
