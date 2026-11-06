package com.project1.api_guardian.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceResponse {

    private final Long id;
    private final String serviceName;
    private final Long projectId;
}
