package com.flashlink.demoflashlink_url_service.service;

import com.flashlink.demoflashlink_url_service.model.AnalyticsEvent;
import com.flashlink.demoflashlink_url_service.repository.UrlMappingRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsConsumerService {
    
    private final UrlMappingRepository urlMappingRepository;
    private final MeterRegistry meterRegistry;
    private final Counter analyticsEventCounter;
    private final Counter redirectCounter;
    
    public AnalyticsConsumerService(UrlMappingRepository urlMappingRepository, MeterRegistry meterRegistry) {
        this.urlMappingRepository = urlMappingRepository;
        this.meterRegistry = meterRegistry;
        this.analyticsEventCounter = Counter.builder("analytics.events.processed")
                .description("Total number of analytics events processed")
                .register(meterRegistry);
        this.redirectCounter = Counter.builder("redirects.processed")
                .description("Total number of redirects processed")
                .register(meterRegistry);
    }
    
    @KafkaListener(
        topics = "${kafka.topics.analytics-events}",
        groupId = "url-service-analytics",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAnalyticsEvent(
            @Payload AnalyticsEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Processing analytics event: {} for shortCode: {}", 
                    event.getEventType(), event.getShortCode());
            
            switch (AnalyticsEvent.EventType.valueOf(event.getEventType())) {
                case REDIRECT:
                    handleRedirectEvent(event);
                    redirectCounter.increment();
                    break;
                case LINK_CREATED:
                    handleLinkCreatedEvent(event);
                    break;
                case LINK_EXPIRED:
                    handleLinkExpiredEvent(event);
                    break;
                case LINK_DELETED:
                    handleLinkDeletedEvent(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
            
            analyticsEventCounter.increment(
                    "event_type", event.getEventType(),
                    "short_code", event.getShortCode()
            );
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed analytics event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Error processing analytics event: {}", event.getEventId(), e);
            // In production, consider dead letter queue handling
            acknowledgment.acknowledge();
        }
    }
    
    private void handleRedirectEvent(AnalyticsEvent event) {
        urlMappingRepository.findByShortCode(event.getShortCode())
                .ifPresent(mapping -> {
                    mapping.setRedirectCount(mapping.getRedirectCount() + 1);
                    mapping.setLastRedirectAt(LocalDateTime.now());
                    urlMappingRepository.save(mapping);
                    
                    meterRegistry.counter("redirects.by_short_code", "short_code", event.getShortCode())
                            .increment();
                    
                    log.debug("Updated redirect count for {}: {}", 
                            event.getShortCode(), mapping.getRedirectCount());
                });
    }
    
    private void handleLinkCreatedEvent(AnalyticsEvent event) {
        meterRegistry.counter("links.created", "owner_id", 
                event.getMetadata() != null ? event.getMetadata().get("ownerId").toString() : "unknown")
                .increment();
        log.debug("Link created event processed for: {}", event.getShortCode());
    }
    
    private void handleLinkExpiredEvent(AnalyticsEvent event) {
        meterRegistry.counter("links.expired").increment();
        log.debug("Link expired event processed for: {}", event.getShortCode());
    }
    
    private void handleLinkDeletedEvent(AnalyticsEvent event) {
        meterRegistry.counter("links.deleted").increment();
        log.debug("Link deleted event processed for: {}", event.getShortCode());
    }
}
