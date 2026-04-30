package com.teamtask.repository;

import com.teamtask.model.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByMembersEmail(String email);
}
