package com.teamtask.dto;

import com.teamtask.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDtos {
    public record TaskRequest(
            @NotBlank String title,
            String description,
            @NotNull Long assignedToId,
            @NotNull Long projectId,
            LocalDate deadline
    ) {
    }

    public record TaskStatusRequest(@NotNull TaskStatus status) {
    }

    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            Long assignedToId,
            String assignedToName,
            Long projectId,
            String projectName,
            LocalDate deadline,
            LocalDateTime createdAt
    ) {
    }
}
