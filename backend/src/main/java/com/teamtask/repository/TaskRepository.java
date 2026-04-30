package com.teamtask.repository;

import com.teamtask.model.Task;
import com.teamtask.model.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToEmail(String email);
    long countByStatus(TaskStatus status);
    long countByDeadlineBeforeAndStatusNot(LocalDate date, TaskStatus status);
    long countByAssignedToEmailAndStatus(String email, TaskStatus status);
    long countByAssignedToEmailAndDeadlineBeforeAndStatusNot(String email, LocalDate date, TaskStatus status);
}
