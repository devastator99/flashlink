package com.flashlink.demoflashlink_url_service.scheduler;

import com.flashlink.demoflashlink_url_service.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final UrlMappingRepository repository;

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupExpiredUrls() {
        log.info("Starting cleanup of expired URLs");
        try {
            LocalDateTime now = LocalDateTime.now();
            repository.deleteExpiredMappings(now);
            log.info("Completed cleanup of expired URLs");
        } catch (Exception e) {
            log.error("Error during expired URL cleanup", e);
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes for cache warming
    public void logCacheStats() {
        log.debug("Cache warming task executed");
    }
}
