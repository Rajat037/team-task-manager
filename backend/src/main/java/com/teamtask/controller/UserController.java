package com.teamtask.controller;

import com.teamtask.dto.AuthDtos.UserResponse;
import com.teamtask.repository.UserRepository;
import com.teamtask.service.CurrentUserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/profile")
    public UserResponse profile() {
        return toResponse(currentUserService.get());
    }

    @GetMapping("/team")
    public List<UserResponse> team() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(com.teamtask.model.User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
