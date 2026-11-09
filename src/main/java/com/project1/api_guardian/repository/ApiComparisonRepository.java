package com.project1.api_guardian.repository;

import com.project1.api_guardian.entity.ApiComparison;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiComparisonRepository extends JpaRepository<ApiComparison, Long> {
}
