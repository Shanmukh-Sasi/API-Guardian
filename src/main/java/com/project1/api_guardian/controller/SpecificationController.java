package com.project1.api_guardian.controller;

import com.project1.api_guardian.dto.ApiResponse;
import com.project1.api_guardian.dto.SpecificationDetailResponse;
import com.project1.api_guardian.dto.SpecificationResponse;
import com.project1.api_guardian.service.SpecificationManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpecificationController {

    private final SpecificationManagementService specificationManagementService;

    @PostMapping(value = "/services/{serviceId}/specifications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SpecificationResponse>> upload(@PathVariable Long serviceId, @RequestParam("file") MultipartFile file) {
        SpecificationResponse specification = specificationManagementService.upload(serviceId, file);
        return ResponseEntity.created(URI.create("/api/specifications/" + specification.getId()))
                .body(ApiResponse.success("API specification uploaded", specification));
    }

    @GetMapping("/services/{serviceId}/specifications")
    public ResponseEntity<ApiResponse<List<SpecificationResponse>>> listByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Service specifications retrieved",
                specificationManagementService.listByService(serviceId)
        ));
    }

    @GetMapping("/specifications/{id}")
    public ResponseEntity<ApiResponse<SpecificationDetailResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "API specification retrieved",
                specificationManagementService.get(id)
        ));
    }
}
