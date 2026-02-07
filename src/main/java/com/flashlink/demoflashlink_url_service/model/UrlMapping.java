package com.flashlink.demoflashlink_url_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_mapping", indexes = {
    @Index(name = "idx_short_code", columnList = "shortCode"),
    @Index(name = "idx_owner_id", columnList = "ownerId"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_expiry_at", columnList = "expiryAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping {
    @Id
    private Long id; // Snowflake ID

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiry_at")
    private LocalDateTime expiryAt;
    
    @Column(name = "owner_id")
    private String ownerId;
    
    @Column(name = "redirect_count")
    private Long redirectCount = 0L;
    
    @Column(name = "ttl_seconds")
    private Integer ttlSeconds;
    
    @Column(name = "last_redirect_at")
    private LocalDateTime lastRedirectAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata for extensibility
}
