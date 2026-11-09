package com.project1.api_guardian.service;

import com.project1.api_guardian.comparison.OpenApiComparisonEngine;
import com.project1.api_guardian.dto.CompareVersionsRequest;
import com.project1.api_guardian.dto.ComparisonChangeResponse;
import com.project1.api_guardian.dto.ComparisonResponse;
import com.project1.api_guardian.entity.ApiComparison;
import com.project1.api_guardian.entity.ApiSpecification;
import com.project1.api_guardian.exception.ConflictException;
import com.project1.api_guardian.exception.ResourceNotFoundException;
import com.project1.api_guardian.parser.OpenApiSpecificationParser;
import com.project1.api_guardian.repository.ApiComparisonRepository;
import com.project1.api_guardian.repository.ApiSpecificationRepository;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ComparisonManagementService {

    private final ApiSpecificationRepository specificationRepository;
    private final ApiComparisonRepository comparisonRepository;
    private final OpenApiSpecificationParser specificationParser;
    private final OpenApiComparisonEngine comparisonEngine;
    private final ObjectMapper objectMapper;

    public ComparisonResponse compare(Long serviceId, CompareVersionsRequest request) {
        String baselineVersion = request.getBaselineVersion().trim();
        String targetVersion = request.getTargetVersion().trim();
        if (baselineVersion.equals(targetVersion)) {
            throw new ConflictException("Choose two different specification versions to compare");
        }

        ApiSpecification baseline = findSpecification(serviceId, baselineVersion);
        ApiSpecification target = findSpecification(serviceId, targetVersion);

        OpenAPI baselineOpenApi = specificationParser.parseAndValidate(baseline.getDefinition());
        OpenAPI targetOpenApi = specificationParser.parseAndValidate(target.getDefinition());
        List<ComparisonChangeResponse> changes = comparisonEngine.compare(baselineOpenApi, targetOpenApi);
        JsonNode changesTree = objectMapper.valueToTree(changes);

        ApiComparison comparison = ApiComparison.create(
                baseline,
                target,
                changesTree.toString()
        );
        ApiComparison savedComparison = comparisonRepository.save(comparison);
        return toResponse(savedComparison, changesTree);
    }

    @Transactional(readOnly = true)
    public ComparisonResponse get(Long comparisonId) {
        ApiComparison comparison = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new ResourceNotFoundException("API comparison", comparisonId));
        return toResponse(comparison, objectMapper.readTree(comparison.getChangesJson()));
    }

    private ApiSpecification findSpecification(Long serviceId, String version) {
        return specificationRepository.findByService_IdAndVersion(serviceId, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Specification version " + version + " was not found for service " + serviceId
                ));
    }

    private ComparisonResponse toResponse(ApiComparison comparison, JsonNode changes) {
        ApiSpecification baseline = comparison.getBaselineSpecification();
        ApiSpecification target = comparison.getTargetSpecification();
        return new ComparisonResponse(
                comparison.getId(),
                baseline.getService().getId(),
                baseline.getVersion(),
                target.getVersion(),
                comparison.getComparedAt(),
                changes
        );
    }
}
