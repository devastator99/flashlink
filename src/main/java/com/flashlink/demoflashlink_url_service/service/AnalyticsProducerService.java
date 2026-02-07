package com.flashlink.demoflashlink_url_service.service;

import com.flashlink.demoflashlink_url_service.model.AnalyticsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsProducerService {
    
    private final KafkaTemplate<String, AnalyticsEvent> kafkaTemplate;
    
    @Value("${kafka.topics.analytics-events}")
    private String analyticsTopic;
    
    public void publishRedirectEvent(String shortCode, String longUrl, String clientIp, 
                                   String userAgent, String referer) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .shortCode(shortCode)
                .longUrl(longUrl)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .referer(referer)
                .timestamp(java.time.LocalDateTime.now())
                .eventType(AnalyticsEvent.EventType.REDIRECT.name())
                .build();
                
        try {
            kafkaTemplate.send(analyticsTopic, shortCode, event)
                    .addCallback(
                            result -> log.debug("Analytics event published: {}", event.getEventId()),
                            failure -> log.error("Failed to publish analytics event: {}", event.getEventId(), failure)
                    );
        } catch (Exception e) {
            log.error("Error publishing analytics event", e);
        }
    }
    
    public void publishLinkCreatedEvent(String shortCode, String longUrl, String ownerId) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .shortCode(shortCode)
                .longUrl(longUrl)
                .timestamp(java.time.LocalDateTime.now())
                .eventType(AnalyticsEvent.EventType.LINK_CREATED.name())
                .metadata(java.util.Map.of("ownerId", ownerId))
                .build();
                
        try {
            kafkaTemplate.send(analyticsTopic, shortCode, event);
        } catch (Exception e) {
            log.error("Error publishing link created event", e);
        }
    }
}
