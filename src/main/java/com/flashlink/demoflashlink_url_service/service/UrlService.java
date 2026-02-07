package com.flashlink.demoflashlink_url_service.service;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.repository.UrlMappingRepository;
import com.flashlink.demoflashlink_url_service.util.Base62Encoder;
import com.flashlink.demoflashlink_url_service.util.SnowflakeIdGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlMappingRepository repository;
    private final SnowflakeIdGenerator idGenerator;
    private final Base62Encoder base62;
    private final AnalyticsProducerService analyticsProducerService;
    private final Timer urlShorteningTimer;
    private static final int MAX_RETRIES = 3;
    private static final int DEFAULT_EXPIRY_DAYS = 30;

    @Transactional
    @CacheEvict(value = "urlMapping", allEntries = true)
    public UrlMapping shortenUrl(String longUrl) {
        return shortenUrl(longUrl, null, null);
    }

    @Transactional
    @CacheEvict(value = "urlMapping", allEntries = true)
    public UrlMapping shortenUrl(String longUrl, LocalDateTime expiryAt) {
        return shortenUrl(longUrl, expiryAt, null);
    }

    @Transactional
    @CacheEvict(value = "urlMapping", allEntries = true)
    public UrlMapping shortenUrl(String longUrl, LocalDateTime expiryAt, String ownerId) {
        return Timer.Sample.start(urlShorteningTimer)
                .stopCallable(() -> performUrlShortening(longUrl, expiryAt, ownerId));
    }

    private UrlMapping performUrlShortening(String longUrl, LocalDateTime expiryAt, String ownerId) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                long id = idGenerator.nextId();
                String code = base62.encode(id);
                
                if (repository.existsByShortCode(code)) {
                    log.warn("Collision detected for short code: {} on attempt {}", code, attempt + 1);
                    continue;
                }
                
                LocalDateTime expiry = expiryAt != null ? expiryAt : 
                    LocalDateTime.now().plus(DEFAULT_EXPIRY_DAYS, ChronoUnit.DAYS);
                
                Integer ttlSeconds = expiryAt != null ? 
                    (int) ChronoUnit.SECONDS.between(LocalDateTime.now(), expiryAt) : null;
                
                UrlMapping mapping = UrlMapping.builder()
                        .id(id)
                        .shortCode(code)
                        .longUrl(longUrl)
                        .createdAt(LocalDateTime.now())
                        .expiryAt(expiry)
                        .ownerId(ownerId)
                        .ttlSeconds(ttlSeconds)
                        .redirectCount(0L)
                        .build();
                
                UrlMapping result = repository.save(mapping);
                log.info("Created URL mapping: {} -> {}", code, longUrl);
                
                // Emit analytics event for link creation
                analyticsProducerService.publishLinkCreatedEvent(code, longUrl, ownerId);
                
                return result;
                
            } catch (Exception e) {
                log.warn("Attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt == MAX_RETRIES - 1) {
                    throw new IllegalStateException("Failed to generate unique short code after " + MAX_RETRIES + " attempts", e);
                }
            }
        }
        throw new IllegalStateException("Unexpected error in URL shortening");
    }

    @Cacheable(value = "urlMapping", key = "#shortCode")
    public Optional<UrlMapping> getUrlMapping(String shortCode) {
        return repository.findByShortCode(shortCode)
                .filter(mapping -> mapping.getExpiryAt() == null || mapping.getExpiryAt().isAfter(LocalDateTime.now()));
    }

    @Cacheable(value = "longUrl", key = "#shortCode")
    public Optional<String> getLongUrl(String shortCode) {
        return getUrlMapping(shortCode)
                .map(UrlMapping::getLongUrl);
    }
}
