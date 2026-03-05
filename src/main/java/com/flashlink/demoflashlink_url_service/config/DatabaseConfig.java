package com.flashlink.demoflashlink_url_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = "com.flashlink.demoflashlink_url_service.repository")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class DatabaseConfig {

    public static class SystemAuditorAware implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.of("system");
        }
    }

    @org.springframework.context.annotation.Bean(name = "auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return new SystemAuditorAware();
    }
}
