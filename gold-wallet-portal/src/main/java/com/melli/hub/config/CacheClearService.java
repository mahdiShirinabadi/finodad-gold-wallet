package com.melli.hub.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class CacheClearService {
    @Autowired
    private CacheManager cacheManager;

    @Value("${cache.reset.names}")
    private String cacheNames; // This will hold the cache names from properties

    @PostConstruct
    public void clearCache() {
        // Split the cache names from properties and clear each specified cache
        List<String> cachesToReset = Arrays.asList(cacheNames.split(","));
        cachesToReset.forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
                log.info("Cleared cache: ({})", cacheName);
            } else {
                log.error("Cache not found: ({})", cacheName);
            }
        });
    }
}
