package com.project1.api_guardian.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SpecificationDetailResponse {

    private final Long id;
    private final Long serviceId;
    private final String title;
    private final String version;
    private final String openApiVersion;
    private final String format;
    private final long fileSizeBytes;
    private final Instant uploadedAt;
    private final String definition;
}
