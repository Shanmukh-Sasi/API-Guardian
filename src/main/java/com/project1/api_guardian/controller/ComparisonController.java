package com.project1.api_guardian.controller;

import com.project1.api_guardian.dto.ApiResponse;
import com.project1.api_guardian.dto.CompareVersionsRequest;
import com.project1.api_guardian.dto.ComparisonResponse;
import com.project1.api_guardian.service.ComparisonManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonManagementService comparisonManagementService;

    @PostMapping("/services/{serviceId}/comparisons")
    public ResponseEntity<ApiResponse<ComparisonResponse>> compare(
            @PathVariable Long serviceId,
            @Valid @RequestBody CompareVersionsRequest request
    ) {
        ComparisonResponse comparison = comparisonManagementService.compare(serviceId, request);
        return ResponseEntity.created(URI.create("/api/comparisons/" + comparison.getId()))
                .body(ApiResponse.success("API specifications compared", comparison));
    }

    @GetMapping("/comparisons/{id}")
    public ResponseEntity<ApiResponse<ComparisonResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "API comparison retrieved",
                comparisonManagementService.get(id)
        ));
    }
}
