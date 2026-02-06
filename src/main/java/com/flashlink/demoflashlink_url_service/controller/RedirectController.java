package com.flashlink.demoflashlink_url_service.controller;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final UrlMappingRepository repository;

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    public String redirectToLongUrl(@PathVariable String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode);
        if (mapping != null) {
            return "redirect:" + mapping.getLongUrl();
        }
        // If short code not found, redirect to home page
        return "forward:/index.html";
    }
}
