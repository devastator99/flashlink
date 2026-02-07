package com.flashlink.demoflashlink_url_service.controller;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.service.RateLimitService;
import com.flashlink.demoflashlink_url_service.service.UrlService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class UrlApiController {

    private final UrlService urlService;
    private final RateLimitService rateLimitService;

    @PostMapping("/shorten")
    @RateLimiter(name = "shorten", fallbackMethod = "shortenFallback")
    public ResponseEntity<Map<String, String>> shortenUrl(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIp(httpRequest);
        if (!rateLimitService.isAllowed(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new RateLimitExceededException("Rate limit exceeded");
        }

        UrlMapping mapping = urlService.shortenUrl(request.url(), request.expiryAt());
        
        Map<String, String> response = Map.of(
            "shortCode", mapping.getShortCode(),
            "shortUrl", getBaseUrl(httpRequest) + "/" + mapping.getShortCode(),
            "longUrl", mapping.getLongUrl(),
            "createdAt", mapping.getCreatedAt().toString(),
            "expiryAt", mapping.getExpiryAt() != null ? mapping.getExpiryAt().toString() : null
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expand/{shortCode}")
    @RateLimiter(name = "expand", fallbackMethod = "expandFallback")
    public ResponseEntity<Map<String, Object>> expandUrl(@PathVariable String shortCode) {
        return urlService.getUrlMapping(shortCode)
                .map(mapping -> {
                    Map<String, Object> response = Map.of(
                        "shortCode", mapping.getShortCode(),
                        "longUrl", mapping.getLongUrl(),
                        "createdAt", mapping.getCreatedAt().toString(),
                        "expiryAt", mapping.getExpiryAt() != null ? mapping.getExpiryAt().toString() : null,
                        "expired", mapping.getExpiryAt() != null && mapping.getExpiryAt().isBefore(LocalDateTime.now())
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        } else {
            return scheme + "://" + serverName + ":" + serverPort;
        }
    }

    public ResponseEntity<Map<String, String>> shortenFallback(ShortenRequest request, HttpServletRequest httpRequest, Exception ex) {
        log.warn("Rate limit fallback triggered for IP: {}", getClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded. Please try again later."));
    }

    public ResponseEntity<Map<String, Object>> expandFallback(String shortCode, Exception ex) {
        log.warn("Rate limit fallback triggered for expand: {}", shortCode);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded. Please try again later."));
    }

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }

    public record ShortenRequest(
            @NotBlank @Size(max = 2048) String url,
            LocalDateTime expiryAt
    ) {}
}
