package com.flashlink.demoflashlink_url_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashlink.demoflashlink_url_service.dto.UrlRequest;
import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UrlControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;

    @InjectMocks
    private UrlController urlController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(urlController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shorten_ShouldReturnShortCode_WhenValidUrl() throws Exception {
        // Given
        String longUrl = "https://example.com";
        UrlRequest request = new UrlRequest();
        request.setLongUrl(longUrl);

        UrlMapping mapping = UrlMapping.builder()
                .id(12345L)
                .shortCode("abc123")
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now())
                .build();

        when(urlService.shortenUrl(longUrl)).thenReturn(mapping);

        // When & Then
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shorten_ShouldReturnBadRequest_WhenInvalidUrl() throws Exception {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("invalid-url");

        // When & Then
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shorten_ShouldReturnBadRequest_WhenEmptyUrl() throws Exception {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("");

        // When & Then
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shorten_ShouldReturnBadRequest_WhenNullUrl() throws Exception {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl(null);

        // When & Then
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shorten_ShouldReturnBadRequest_WhenRequestBodyMissing() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shorten_ShouldHandleServiceException() throws Exception {
        // Given
        String longUrl = "https://example.com";
        UrlRequest request = new UrlRequest();
        request.setLongUrl(longUrl);

        when(urlService.shortenUrl(anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shorten_ShouldReturnCorrectContentType() throws Exception {
        // Given
        String longUrl = "https://example.com";
        UrlRequest request = new UrlRequest();
        request.setLongUrl(longUrl);

        UrlMapping mapping = UrlMapping.builder()
                .id(12345L)
                .shortCode("abc123")
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now())
                .build();

        when(urlService.shortenUrl(longUrl)).thenReturn(mapping);

        // When & Then
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
