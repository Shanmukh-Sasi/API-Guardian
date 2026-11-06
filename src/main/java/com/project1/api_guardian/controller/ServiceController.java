package com.project1.api_guardian.controller;

import com.project1.api_guardian.dto.ApiResponse;
import com.project1.api_guardian.dto.CreateServiceRequest;
import com.project1.api_guardian.dto.ServiceResponse;
import com.project1.api_guardian.dto.UpdateServiceRequest;
import com.project1.api_guardian.service.ServiceManagementService;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceManagementService serviceManagementService;

    @PostMapping("/projects/{projectId}/services")
    public ResponseEntity<ApiResponse<ServiceResponse>> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateServiceRequest request
    ) {
        ServiceResponse service = serviceManagementService.create(projectId, request);
        return ResponseEntity.created(URI.create("/api/services/" + service.getId()))
                .body(ApiResponse.success("Service created", service));
    }

    @GetMapping("/projects/{projectId}/services")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> listByProject(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Project services retrieved",
                serviceManagementService.listByProject(projectId)
        ));
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Service retrieved",
                serviceManagementService.get(id)
        ));
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Service updated",
                serviceManagementService.update(id, request)
        ));
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        serviceManagementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted", null));
    }
}
