package com.teamtask.dto;

import com.teamtask.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record SignupRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 8) String password,
            Role role
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }

    public record UserResponse(Long id, String name, String email, Role role) {
    }
}
