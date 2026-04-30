package com.teamtask.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public class ProjectDtos {
    public record ProjectRequest(
            @NotBlank String name,
            String description,
            Set<Long> memberIds
    ) {
    }

    public record ProjectResponse(
            Long id,
            String name,
            String description,
            String createdBy,
            int memberCount,
            Set<Long> memberIds
    ) {
    }
}
