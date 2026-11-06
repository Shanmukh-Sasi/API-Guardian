package com.project1.api_guardian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateServiceRequest {

    @NotBlank
    @Size(max = 120)
    private String serviceName;
}
