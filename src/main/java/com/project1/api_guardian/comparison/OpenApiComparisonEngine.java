package com.project1.api_guardian.comparison;

import com.project1.api_guardian.dto.ComparisonChangeResponse;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenApiComparisonEngine {

    private final EndpointComparisonService endpointComparisonService;
    private final SchemaComparisonService schemaComparisonService;

    public List<ComparisonChangeResponse> compare(OpenAPI baseline, OpenAPI target) {
        List<ComparisonChangeResponse> changes = new ArrayList<>(
                endpointComparisonService.compare(baseline, target)
        );
        changes.addAll(schemaComparisonService.compare(baseline, target));
        return changes;
    }
}
