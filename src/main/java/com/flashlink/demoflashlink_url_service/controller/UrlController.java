package com.flashlink.demoflashlink_url_service.controller;

import com.flashlink.demoflashlink_url_service.dto.UrlRequest;
import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/shorten")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService service;

    @PostMapping
    public ResponseEntity<Map<String, Object>> shorten(@Valid @RequestBody UrlRequest dto) {
        UrlMapping mapping = service.shortenUrl(dto.getLongUrl());
        Map<String, Object> response = Map.of(
            "shortCode", mapping.getShortCode(),
            "createdAt", mapping.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }
}
