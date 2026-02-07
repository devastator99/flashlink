package com.flashlink.demoflashlink_url_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {
    private String eventId;
    private String shortCode;
    private String longUrl;
    private String clientIp;
    private String userAgent;
    private String referer;
    private String country;
    private String city;
    private LocalDateTime timestamp;
    private String eventType;
    private Map<String, Object> metadata;
    
    public enum EventType {
        REDIRECT,
        LINK_CREATED,
        LINK_EXPIRED,
        LINK_DELETED
    }
}
