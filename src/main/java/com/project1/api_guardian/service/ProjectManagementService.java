package com.project1.api_guardian.service;

import com.project1.api_guardian.dto.CreateProjectRequest;
import com.project1.api_guardian.dto.ProjectResponse;
import com.project1.api_guardian.dto.UpdateProjectRequest;
import com.project1.api_guardian.entity.Project;
import com.project1.api_guardian.entity.User;
import com.project1.api_guardian.exception.ConflictException;
import com.project1.api_guardian.exception.ResourceNotFoundException;
import com.project1.api_guardian.repository.ProjectRepository;
import com.project1.api_guardian.repository.ServiceRepository;
import com.project1.api_guardian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectManagementService {

    private final ProjectRepository projectRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ProjectResponse create(CreateProjectRequest request) {
        User owner = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Project project = Project.create(request.getName(), request.getDescription(), owner);
        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(Long userId) {
        List<Project> projects = userId == null
                ? projectRepository.findAll()
                : projectRepository.findAllByUser_Id(userId);
        return projects.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) {
        return toResponse(findProject(id));
    }

    public ProjectResponse update(Long id, UpdateProjectRequest request) {
        Project project = findProject(id);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return toResponse(projectRepository.save(project));
    }

    public void delete(Long id) {
        Project project = findProject(id);
        if (serviceRepository.existsByProject_Id(id)) {
            throw new ConflictException("Remove the project's services before deleting the project");
        }
        projectRepository.delete(project);
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getUser().getId()
        );
    }
}
