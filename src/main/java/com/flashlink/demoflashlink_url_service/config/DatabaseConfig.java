package com.flashlink.demoflashlink_url_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = "com.flashlink.demoflashlink_url_service.repository")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EntityScan(basePackages = "com.flashlink.demoflashlink_url_service.model")
public class DatabaseConfig {

    public static class SystemAuditorAware implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.of("system");
        }
    }
}
