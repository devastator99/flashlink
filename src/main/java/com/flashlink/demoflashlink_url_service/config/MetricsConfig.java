package com.flashlink.demoflashlink_url_service.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "url-service");
    }

    @Bean
    public Timer urlShorteningTimer(MeterRegistry meterRegistry) {
        return Timer.builder("url.shortening.duration")
                .description("Time taken to shorten URLs")
                .register(meterRegistry);
    }

    @Bean
    public Timer redirectTimer(MeterRegistry meterRegistry) {
        return Timer.builder("redirect.duration")
                .description("Time taken to process redirects")
                .register(meterRegistry);
    }

    @Bean
    public Timer cacheAccessTimer(MeterRegistry meterRegistry) {
        return Timer.builder("cache.access.duration")
                .description("Time taken for cache operations")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseAccessTimer(MeterRegistry meterRegistry) {
        return Timer.builder("database.access.duration")
                .description("Time taken for database operations")
                .register(meterRegistry);
    }

    @Bean
    public RedisTemplate<String, Long> redisMetricsTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
