package com.project1.api_guardian.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonChangeResponse {

    private String changeType;
    private String path;
    private String method;
    private String element;
    private String oldValue;
    private String newValue;
    private boolean breaking;
}
