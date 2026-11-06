package com.project1.api_guardian.controller;

import com.project1.api_guardian.dto.ApiResponse;
import com.project1.api_guardian.dto.CreateProjectRequest;
import com.project1.api_guardian.dto.ProjectResponse;
import com.project1.api_guardian.dto.UpdateProjectRequest;
import com.project1.api_guardian.service.ProjectManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectManagementService projectManagementService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse project = projectManagementService.create(request);
        return ResponseEntity.created(URI.create("/api/projects/" + project.getId()))
                .body(ApiResponse.success("Project created", project));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> list(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Projects retrieved",
                projectManagementService.list(userId)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Project retrieved",
                projectManagementService.get(id)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Project updated",
                projectManagementService.update(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectManagementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }
}
