package com.project1.api_guardian.repository;

import com.project1.api_guardian.entity.ApiSpecification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiSpecificationRepository extends JpaRepository<ApiSpecification, Long> {
    boolean existsByService_IdAndVersion(Long serviceId, String version);

    boolean existsByService_Id(Long serviceId);

    List<ApiSpecification> findAllByService_IdOrderByUploadedAtDesc(Long serviceId);
}
