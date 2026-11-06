package com.project1.api_guardian.repository;

import com.project1.api_guardian.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUser_Id(Long userId);
}
