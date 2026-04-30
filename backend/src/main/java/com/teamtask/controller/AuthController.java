package com.teamtask.controller;

import com.teamtask.dto.AuthDtos.AuthResponse;
import com.teamtask.dto.AuthDtos.LoginRequest;
import com.teamtask.dto.AuthDtos.SignupRequest;
import com.teamtask.dto.AuthDtos.UserResponse;
import com.teamtask.model.Role;
import com.teamtask.model.User;
import com.teamtask.repository.UserRepository;
import com.teamtask.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.MEMBER : request.role());
        userRepository.save(user);
        return ResponseEntity.ok(response(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.email().toLowerCase())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .map(user -> ResponseEntity.ok(response(user)))
                .orElseGet(() -> ResponseEntity.status(401).body(new AuthResponse("", null)));
    }

    private AuthResponse response(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
