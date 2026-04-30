package com.teamtask.config;

import com.teamtask.model.Project;
import com.teamtask.model.Role;
import com.teamtask.model.Task;
import com.teamtask.model.User;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.TaskRepository;
import com.teamtask.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@teamtask.local");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);

            User member = new User();
            member.setName("Member User");
            member.setEmail("member@teamtask.local");
            member.setPassword(passwordEncoder.encode("Member@123"));
            member.setRole(Role.MEMBER);

            userRepository.save(admin);
            userRepository.save(member);

            Project project = new Project();
            project.setName("Website Launch");
            project.setDescription("Plan and launch the public website.");
            project.setCreatedBy(admin);
            project.getMembers().add(admin);
            project.getMembers().add(member);
            projectRepository.save(project);

            Task task = new Task();
            task.setTitle("Create landing page content");
            task.setDescription("Draft hero copy and feature sections.");
            task.setAssignedTo(member);
            task.setProject(project);
            task.setDeadline(LocalDate.now().plusDays(5));
            taskRepository.save(task);
        };
    }
}
