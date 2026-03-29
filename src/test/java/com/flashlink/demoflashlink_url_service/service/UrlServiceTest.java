package com.flashlink.demoflashlink_url_service.service;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.repository.UrlMappingRepository;
import com.flashlink.demoflashlink_url_service.util.Base62Encoder;
import com.flashlink.demoflashlink_url_service.util.SnowflakeIdGenerator;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private SnowflakeIdGenerator idGenerator;

    @Mock
    private Base62Encoder base62;

    @Mock
    private AnalyticsProducerService analyticsProducerService;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        // Initialize mocks with default behavior to reduce boilerplate
        lenient().when(idGenerator.nextId()).thenReturn(12345L);
        lenient().when(base62.encode(12345L)).thenReturn("abc123");
        lenient().when(repository.existsByShortCode("abc123")).thenReturn(false);
        lenient().when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            if (mapping.getCreatedAt() == null) {
                mapping.setCreatedAt(LocalDateTime.now());
            }
            return mapping;
        });
    }

    @Test
    void shortenUrl_ShouldCreateMapping_WhenValidUrl() {
        // Given
        String longUrl = "https://example.com";

        // When
        UrlMapping result = urlService.shortenUrl(longUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLongUrl()).isEqualTo(longUrl);
        assertThat(result.getShortCode()).isEqualTo("abc123");
        assertThat(result.getId()).isEqualTo(12345L);
        assertThat(result.getRedirectCount()).isEqualTo(0L);

        verify(repository).save(any(UrlMapping.class));
        verify(analyticsProducerService).publishLinkCreatedEvent("abc123", longUrl, null);
    }

    @Test
    void shortenUrl_ShouldRetryOnCollision_WhenShortCodeExists() {
        // Given
        String longUrl = "https://example.com";
        long id1 = 12345L, id2 = 12346L;
        String shortCode1 = "abc123", shortCode2 = "def456";
        LocalDateTime now = LocalDateTime.now();

        when(idGenerator.nextId()).thenReturn(id1).thenReturn(id2);
        when(base62.encode(id1)).thenReturn(shortCode1);
        when(base62.encode(id2)).thenReturn(shortCode2);
        when(repository.existsByShortCode(shortCode1)).thenReturn(true);
        when(repository.existsByShortCode(shortCode2)).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            mapping.setCreatedAt(now);
            return mapping;
        });

        // When
        UrlMapping result = urlService.shortenUrl(longUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getShortCode()).isEqualTo(shortCode2);
        assertThat(result.getId()).isEqualTo(id2);

        verify(repository, times(2)).existsByShortCode(anyString());
        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void shortenUrl_ShouldThrowException_WhenMaxRetriesExceeded() {
        // Given
        String longUrl = "https://example.com";
        when(idGenerator.nextId()).thenReturn(12345L);
        when(base62.encode(12345L)).thenReturn("abc123");
        when(repository.existsByShortCode("abc123")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> urlService.shortenUrl(longUrl))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to generate unique short code");

        verify(repository, times(3)).existsByShortCode("abc123");
        verify(repository, never()).save(any());
    }

    @Test
    void shortenUrl_WithExpiryDate_ShouldSetExpiry() {
        // Given
        String longUrl = "https://example.com";
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
        long id = 12345L;
        String shortCode = "abc123";
        LocalDateTime now = LocalDateTime.now();

        when(idGenerator.nextId()).thenReturn(id);
        when(base62.encode(id)).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            mapping.setCreatedAt(now);
            return mapping;
        });

        // When
        UrlMapping result = urlService.shortenUrl(longUrl, expiryDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExpiryAt()).isEqualTo(expiryDate);
        assertThat(result.getTtlSeconds()).isNotNull();
    }

    @Test
    void shortenUrl_WithOwnerId_ShouldSetOwner() {
        // Given
        String longUrl = "https://example.com";
        String ownerId = "user123";
        long id = 12345L;
        String shortCode = "abc123";
        LocalDateTime now = LocalDateTime.now();

        when(idGenerator.nextId()).thenReturn(id);
        when(base62.encode(id)).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            mapping.setCreatedAt(now);
            return mapping;
        });

        // When
        UrlMapping result = urlService.shortenUrl(longUrl, null, ownerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        verify(analyticsProducerService).publishLinkCreatedEvent(shortCode, longUrl, ownerId);
    }

    @Test
    void getUrlMapping_ShouldReturnMapping_WhenValidShortCode() {
        // Given
        String shortCode = "abc123";
        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .longUrl("https://example.com")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));

        // When
        Optional<UrlMapping> result = urlService.getUrlMapping(shortCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getShortCode()).isEqualTo(shortCode);
    }

    @Test
    void getUrlMapping_ShouldReturnEmpty_WhenShortCodeNotExists() {
        // Given
        String shortCode = "nonexistent";
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        // When
        Optional<UrlMapping> result = urlService.getUrlMapping(shortCode);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shortenUrl_ShouldHandleNullUrl_WhenGivenNull() {
        // Given
        String longUrl = null;

        // When & Then
        assertThatThrownBy(() -> urlService.shortenUrl(longUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL cannot be null or empty");
    }

    @Test
    void getUrlMapping_ShouldReturnEmpty_WhenUrlExpired() {
        // Given
        String shortCode = "expired";
        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .longUrl("https://example.com")
                .createdAt(LocalDateTime.now().minusDays(2))
                .expiryAt(LocalDateTime.now().minusDays(1))
                .build();

        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));

        // When
        Optional<UrlMapping> result = urlService.getUrlMapping(shortCode);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getLongUrl_ShouldReturnUrl_WhenValidShortCode() {
        // Given
        String shortCode = "abc123";
        String longUrl = "https://example.com";
        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));

        // When
        Optional<String> result = urlService.getLongUrl(shortCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(longUrl);
    }

    @Test
    void getLongUrl_ShouldReturnEmpty_WhenShortCodeNotExists() {
        // Given
        String shortCode = "nonexistent";
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        // When
        Optional<String> result = urlService.getLongUrl(shortCode);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shortenUrl_ShouldHandleDataIntegrityViolation() {
        // Given
        String longUrl = "https://example.com";
        long id = 12345L;
        String shortCode = "abc123";

        when(idGenerator.nextId()).thenReturn(id);
        when(base62.encode(id)).thenReturn(shortCode);
        when(repository.existsByShortCode(shortCode)).thenReturn(false);
        when(repository.save(any(UrlMapping.class))).thenThrow(new DataIntegrityViolationException("Constraint violation"));

        // When & Then
        assertThatThrownBy(() -> urlService.shortenUrl(longUrl))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
