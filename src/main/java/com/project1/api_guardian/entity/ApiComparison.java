package com.project1.api_guardian.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "api_comparisons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baseline_specification_id", nullable = false)
    private ApiSpecification baselineSpecification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_specification_id", nullable = false)
    private ApiSpecification targetSpecification;

    @Column(name = "changes_json", nullable = false, columnDefinition = "LONGTEXT")
    private String changesJson;

    @Column(name = "compared_at", nullable = false, updatable = false)
    private Instant comparedAt;

    public static ApiComparison create(
            ApiSpecification baselineSpecification,
            ApiSpecification targetSpecification,
            String changesJson
    ) {
        ApiComparison comparison = new ApiComparison();
        comparison.baselineSpecification = baselineSpecification;
        comparison.targetSpecification = targetSpecification;
        comparison.changesJson = changesJson;
        comparison.comparedAt = Instant.now();
        return comparison;
    }
}
