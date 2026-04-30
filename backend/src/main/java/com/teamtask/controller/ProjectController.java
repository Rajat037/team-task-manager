package com.teamtask.controller;

import com.teamtask.dto.ProjectDtos.ProjectRequest;
import com.teamtask.dto.ProjectDtos.ProjectResponse;
import com.teamtask.model.Project;
import com.teamtask.model.Role;
import com.teamtask.model.User;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.UserRepository;
import com.teamtask.service.CurrentUserService;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ProjectController(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @Transactional
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        User admin = currentUserService.get();
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setCreatedBy(admin);
        project.getMembers().add(admin);
        if (request.memberIds() != null && !request.memberIds().isEmpty()) {
            project.getMembers().addAll(new HashSet<>(userRepository.findAllById(request.memberIds())));
        }
        return toResponse(projectRepository.save(project));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProjectResponse> all() {
        User user = currentUserService.get();
        List<Project> projects = user.getRole() == Role.ADMIN
                ? projectRepository.findAll()
                : projectRepository.findByMembersEmail(user.getEmail());
        return projects.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ProjectResponse one(@PathVariable Long id) {
        return projectRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @PutMapping("/{id}/members")
    @Transactional
    public ProjectResponse updateMembers(@PathVariable Long id, @RequestBody Set<Long> memberIds) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        User admin = currentUserService.get();
        if (admin.getRole() != Role.ADMIN) {
             throw new IllegalArgumentException("Only admins can modify members");
        }
        project.getMembers().clear();
        project.getMembers().add(admin);
        if (memberIds != null && !memberIds.isEmpty()) {
            project.getMembers().addAll(new HashSet<>(userRepository.findAllById(memberIds)));
        }
        return toResponse(projectRepository.save(project));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedBy().getName(),
                project.getMembers().size(),
                project.getMembers().stream().map(User::getId).collect(Collectors.toSet())
        );
    }
}
