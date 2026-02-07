package com.flashlink.demoflashlink_url_service.repository;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    boolean existsByShortCode(String shortCode);
    Optional<UrlMapping> findByShortCode(String shortCode);
    
    @Query("SELECT u FROM UrlMapping u WHERE u.shortCode = :shortCode AND (u.expiryAt IS NULL OR u.expiryAt > :now)")
    Optional<UrlMapping> findValidByShortCode(@Param("shortCode") String shortCode, @Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM UrlMapping u WHERE u.expiryAt <= :now")
    void deleteExpiredMappings(@Param("now") LocalDateTime now);
}
