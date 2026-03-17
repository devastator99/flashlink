package com.flashlink.demoflashlink_url_service.controller;

import com.flashlink.demoflashlink_url_service.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RedirectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;

    @InjectMocks
    private RedirectController redirectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(redirectController).build();
    }

    @Test
    void redirect_ShouldRedirectToLongUrl_WhenValidShortCode() throws Exception {
        // Given
        String shortCode = "abc123";
        String longUrl = "https://example.com";

        when(urlService.getLongUrl(shortCode)).thenReturn(Optional.of(longUrl));

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(longUrl));
    }

    @Test
    void redirect_ShouldReturnNotFound_WhenShortCodeNotExists() throws Exception {
        // Given
        String shortCode = "nonexistent";

        when(urlService.getLongUrl(shortCode)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_ShouldReturnNotFound_WhenUrlExpired() throws Exception {
        // Given
        String shortCode = "expired";

        when(urlService.getLongUrl(shortCode)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_ShouldHandleSpecialCharacters() throws Exception {
        // Given
        String shortCode = "abc-123_test";
        String longUrl = "https://example.com/path?param=value";

        when(urlService.getLongUrl(shortCode)).thenReturn(Optional.of(longUrl));

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(longUrl));
    }

    @Test
    void redirect_ShouldHandleEmptyShortCode() throws Exception {
        // Given
        String shortCode = "";

        when(urlService.getLongUrl(shortCode)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_ShouldHandleServiceException() throws Exception {
        // Given
        String shortCode = "error";

        when(urlService.getLongUrl(anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void redirect_ShouldPreserveUrlParameters() throws Exception {
        // Given
        String shortCode = "abc123";
        String longUrl = "https://example.com/path";

        when(urlService.getLongUrl(shortCode)).thenReturn(Optional.of(longUrl));

        // When & Then
        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(longUrl));
    }
}
