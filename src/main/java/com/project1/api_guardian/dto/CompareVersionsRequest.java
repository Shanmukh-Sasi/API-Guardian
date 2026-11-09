package com.project1.api_guardian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompareVersionsRequest {

    @NotBlank
    @Size(max = 100)
    private String baselineVersion;

    @NotBlank
    @Size(max = 100)
    private String targetVersion;
}
