package com.flashlink.demoflashlink_url_service.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final DefaultRedisScript<Long> rateLimitScript;
    
    @Value("${rate.limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    @Value("${rate.limit.burst-capacity:10}")
    private int burstCapacity;
    
    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimiterRegistry = RateLimiterRegistry.of(
            RateLimiterConfig.custom()
                .limitForPeriod(requestsPerMinute)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build()
        );
        
        this.rateLimitScript = new DefaultRedisScript<>(
            "local key = KEYS[1]\n" +
            "local capacity = tonumber(ARGV[1])\n" +
            "local tokens = tonumber(ARGV[2])\n" +
            "local interval = tonumber(ARGV[3])\n" +
            "local now = tonumber(ARGV[4])\n" +
            "\n" +
            "local bucket = redis.call('hmget', key, 'tokens', 'last_refill')\n" +
            "local current_tokens = tonumber(bucket[1]) or capacity\n" +
            "local last_refill = tonumber(bucket[2]) or now\n" +
            "\n" +
            "local elapsed = now - last_refill\n" +
            "local tokens_to_add = math.floor(elapsed / interval * tokens)\n" +
            "current_tokens = math.min(capacity, current_tokens + tokens_to_add)\n" +
            "\n" +
            "if current_tokens >= 1 then\n" +
            "    current_tokens = current_tokens - 1\n" +
            "    redis.call('hmset', key, 'tokens', current_tokens, 'last_refill', now)\n" +
            "    redis.call('expire', key, math.ceil(interval * 2))\n" +
            "    return 1\n" +
            "else\n" +
            "    redis.call('hmset', key, 'tokens', current_tokens, 'last_refill', now)\n" +
            "    redis.call('expire', key, math.ceil(interval * 2))\n" +
            "    return 0\n" +
            "end",
            Long.class
        );
    }
    
    public boolean isAllowed(String identifier) {
        try {
            String key = "rate_limit:" + identifier;
            Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(burstCapacity),
                String.valueOf(requestsPerMinute),
                String.valueOf(60.0), // 60 seconds
                String.valueOf(System.currentTimeMillis() / 1000.0)
            );
            return result != null && result == 1;
        } catch (Exception e) {
            log.warn("Rate limiting failed, allowing request: {}", e.getMessage());
            return true; // Fail open
        }
    }
    
    public boolean isAllowedLocal(String identifier) {
        try {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(identifier);
            return rateLimiter.acquirePermission();
        } catch (Exception e) {
            log.warn("Local rate limiting failed, allowing request: {}", e.getMessage());
            return true; // Fail open
        }
    }
}
