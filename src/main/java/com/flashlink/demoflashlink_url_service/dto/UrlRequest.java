package com.flashlink.demoflashlink_url_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UrlRequest {
    @NotBlank
    @Size(max = 2048)
    @Pattern(regexp = "^(http|https)://.*$")
    private String longUrl;
}
