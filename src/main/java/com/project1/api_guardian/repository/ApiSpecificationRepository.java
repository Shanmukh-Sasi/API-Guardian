package com.project1.api_guardian.repository;

import com.project1.api_guardian.entity.ApiSpecification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiSpecificationRepository extends JpaRepository<ApiSpecification, Long> {
    boolean existsByService_IdAndVersion(Long serviceId, String version);

    boolean existsByService_Id(Long serviceId);

    Optional<ApiSpecification> findByService_IdAndVersion(Long serviceId, String version);

    List<ApiSpecification> findAllByService_IdOrderByUploadedAtDesc(Long serviceId);
}
