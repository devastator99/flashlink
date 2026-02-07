package com.flashlink.demoflashlink_url_service.controller;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.service.AnalyticsProducerService;
import com.flashlink.demoflashlink_url_service.service.RateLimitService;
import com.flashlink.demoflashlink_url_service.service.UrlService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;
    private final RateLimitService rateLimitService;
    private final AnalyticsProducerService analyticsProducerService;
    private final Timer redirectTimer;

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    @RateLimiter(name = "redirect", fallbackMethod = "redirectFallback")
    public RedirectView redirectToLongUrl(@PathVariable String shortCode, HttpServletRequest request) {
        return Timer.Sample.start(redirectTimer)
                .stopCallable(() -> performRedirect(shortCode, request));
    }
    
    private RedirectView performRedirect(String shortCode, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        
        if (!rateLimitService.isAllowed(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new RateLimitExceededException("Rate limit exceeded");
        }
        
        Optional<String> longUrl = urlService.getLongUrl(shortCode);
        
        if (longUrl.isPresent()) {
            log.info("Redirecting {} to {}", shortCode, longUrl.get());
            
            // Emit analytics event asynchronously
            analyticsProducerService.publishRedirectEvent(
                    shortCode, 
                    longUrl.get(), 
                    clientIp, 
                    userAgent, 
                    referer
            );
            
            RedirectView redirectView = new RedirectView(longUrl.get());
            redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY); // 301 for SEO
            return redirectView;
        }
        
        log.warn("Short code not found: {}", shortCode);
        return new RedirectView("/");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    public RedirectView redirectFallback(String shortCode, HttpServletRequest request, Exception ex) {
        log.warn("Rate limit fallback triggered for IP: {}", getClientIp(request));
        return new RedirectView("/");
    }
    
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
