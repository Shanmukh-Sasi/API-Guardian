package com.project1.api_guardian.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final Long userId;
}
