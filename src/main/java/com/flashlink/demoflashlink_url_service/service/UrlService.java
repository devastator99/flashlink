package com.flashlink.demoflashlink_url_service.service;

import com.flashlink.demoflashlink_url_service.model.UrlMapping;
import com.flashlink.demoflashlink_url_service.repository.UrlMappingRepository;
import com.flashlink.demoflashlink_url_service.util.Base62Encoder;
import com.flashlink.demoflashlink_url_service.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlMappingRepository repository;
    private final SnowflakeIdGenerator idGenerator;
    private final Base62Encoder base62;

    @Transactional
    public UrlMapping shortenUrl(String longUrl) {
        long id = idGenerator.nextId();
        String code = base62.encode(id);
        if (repository.existsByShortCode(code))
            throw new IllegalStateException("Collision detected!");
        UrlMapping mapping = UrlMapping.builder()
                .id(id)
                .shortCode(code)
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now())
                .build();
        return repository.save(mapping);
    }
}
