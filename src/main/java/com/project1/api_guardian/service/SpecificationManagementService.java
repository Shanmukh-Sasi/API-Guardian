package com.project1.api_guardian.service;

import com.project1.api_guardian.dto.SpecificationDetailResponse;
import com.project1.api_guardian.dto.SpecificationResponse;
import com.project1.api_guardian.entity.ApiSpecification;
import com.project1.api_guardian.entity.Service;
import com.project1.api_guardian.exception.ConflictException;
import com.project1.api_guardian.exception.InvalidSpecificationException;
import com.project1.api_guardian.exception.ResourceNotFoundException;
import com.project1.api_guardian.exception.UploadTooLargeException;
import com.project1.api_guardian.parser.OpenApiSpecificationParser;
import com.project1.api_guardian.repository.ApiSpecificationRepository;
import com.project1.api_guardian.repository.ServiceRepository;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class SpecificationManagementService {

    private static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024;

    private final ApiSpecificationRepository specificationRepository;
    private final ServiceRepository serviceRepository;
    private final OpenApiSpecificationParser specificationParser;

    public SpecificationResponse upload(Long serviceId, MultipartFile file) {
        validateFile(file);
        String format = determineFormat(file.getOriginalFilename());
        String definition = decodeUtf8(file);
        OpenAPI openAPI = specificationParser.parseAndValidate(definition);

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        String title = openAPI.getInfo().getTitle().trim();
        String version = openAPI.getInfo().getVersion().trim();
        if (title.length() > 200 || version.length() > 100 || openAPI.getOpenapi().length() > 20) {
            throw new InvalidSpecificationException("Specification title or version is too long");
        }

        if (specificationRepository.existsByService_IdAndVersion(serviceId, version)) {
            throw new ConflictException("Version " + version + " already exists for this service");
        }

        ApiSpecification specification = ApiSpecification.create(
                title,
                version,
                openAPI.getOpenapi(),
                format,
                file.getSize(),
                definition,
                service
        );
        return toResponse(specificationRepository.save(specification));
    }

    @Transactional(readOnly = true)
    public List<SpecificationResponse> listByService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new ResourceNotFoundException("Service", serviceId);
        }
        return specificationRepository.findAllByService_IdOrderByUploadedAtDesc(serviceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpecificationDetailResponse get(Long id) {
        ApiSpecification specification = specificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API specification", id));
        return toDetailResponse(specification);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidSpecificationException("A non-empty specification file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new UploadTooLargeException("Uploaded file exceeds the 2 MB limit");
        }
    }

    private String determineFormat(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidSpecificationException("The uploaded file must have a .json, .yaml, or .yml extension");
        }

        String filename = originalFilename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new InvalidSpecificationException("Only .json, .yaml, and .yml files are supported");
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "json" -> "JSON";
            case "yaml", "yml" -> "YAML";
            default -> throw new InvalidSpecificationException("Only .json, .yaml, and .yml files are supported");
        };
    }

    private String decodeUtf8(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (IOException exception) {
            throw new InvalidSpecificationException("The specification must be valid UTF-8 text");
        }
    }

    private SpecificationResponse toResponse(ApiSpecification specification) {
        return new SpecificationResponse(
                specification.getId(),
                specification.getService().getId(),
                specification.getTitle(),
                specification.getVersion(),
                specification.getOpenApiVersion(),
                specification.getFormat(),
                specification.getFileSizeBytes(),
                specification.getUploadedAt()
        );
    }

    private SpecificationDetailResponse toDetailResponse(ApiSpecification specification) {
        return new SpecificationDetailResponse(
                specification.getId(),
                specification.getService().getId(),
                specification.getTitle(),
                specification.getVersion(),
                specification.getOpenApiVersion(),
                specification.getFormat(),
                specification.getFileSizeBytes(),
                specification.getUploadedAt(),
                specification.getDefinition()
        );
    }
}
