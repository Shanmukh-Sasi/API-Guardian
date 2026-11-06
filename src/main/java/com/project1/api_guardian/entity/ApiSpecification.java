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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "api_specifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_specification_service_version",
                columnNames = {"service_id", "spec_version"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "spec_version", nullable = false, length = 100)
    private String version;

    @Column(name = "openapi_version", nullable = false, length = 20)
    private String openApiVersion;

    @Column(nullable = false, length = 10)
    private String format;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "definition", nullable = false, columnDefinition = "LONGTEXT")
    private String definition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    public static ApiSpecification create(
            String title,
            String version,
            String openApiVersion,
            String format,
            long fileSizeBytes,
            String definition,
            Service service
    ) {
        ApiSpecification specification = new ApiSpecification();
        specification.title = title;
        specification.version = version;
        specification.openApiVersion = openApiVersion;
        specification.format = format;
        specification.fileSizeBytes = fileSizeBytes;
        specification.uploadedAt = Instant.now();
        specification.definition = definition;
        specification.service = service;
        return specification;
    }
}
