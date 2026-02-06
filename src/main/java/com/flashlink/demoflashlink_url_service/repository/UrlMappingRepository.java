package com.flashlink.demoflashlink_url_service.repository;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    boolean existsByShortCode(String shortCode);
    UrlMapping findByShortCode(String shortCode);
}
