package com.teamtask.controller;

import com.teamtask.model.Role;
import com.teamtask.model.TaskStatus;
import com.teamtask.model.User;
import com.teamtask.repository.TaskRepository;
import com.teamtask.service.CurrentUserService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    public DashboardController(TaskRepository taskRepository, CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Map<String, Long> stats() {
        User user = currentUserService.get();
        if (user.getRole() == Role.ADMIN) {
            long total = taskRepository.count();
            long completed = taskRepository.countByStatus(TaskStatus.DONE);
            long overdue = taskRepository.countByDeadlineBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE);
            return Map.of("totalTasks", total, "completedTasks", completed, "pendingTasks", total - completed, "overdueTasks", overdue);
        }
        long total = taskRepository.findByAssignedToEmail(user.getEmail()).size();
        long completed = taskRepository.countByAssignedToEmailAndStatus(user.getEmail(), TaskStatus.DONE);
        long overdue = taskRepository.countByAssignedToEmailAndDeadlineBeforeAndStatusNot(user.getEmail(), LocalDate.now(), TaskStatus.DONE);
        return Map.of("totalTasks", total, "completedTasks", completed, "pendingTasks", total - completed, "overdueTasks", overdue);
    }
}
