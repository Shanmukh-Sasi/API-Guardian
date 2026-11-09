package com.project1.api_guardian.dto;

import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ComparisonResponse {

    private final Long id;
    private final Long serviceId;
    private final String baselineVersion;
    private final String targetVersion;
    private final Instant comparedAt;
    private final JsonNode changes;
}
