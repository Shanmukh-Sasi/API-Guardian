package com.project1.api_guardian.repository;

import com.project1.api_guardian.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findAllByProject_Id(Long projectId);
    boolean existsByProject_Id(Long projectId);
}
