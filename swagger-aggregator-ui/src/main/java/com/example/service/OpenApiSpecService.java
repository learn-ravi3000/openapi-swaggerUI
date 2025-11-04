package com.example.service;

import com.example.config.AggregatorProperties;
import com.example.config.AggregatorProperties.ServiceDefinition;
import com.example.config.ServiceCatalog;
import com.example.service.model.CachedSpec;
import com.example.service.model.ServiceHealth;
import com.example.service.model.ServiceStatus;
import com.example.service.model.SpecResult;
import com.example.service.model.UpstreamUnavailableException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Fetches upstream OpenAPI documents, maintains a short-lived cache, and tracks per-service health.
 */
@Service
public class OpenApiSpecService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiSpecService.class);

    private final ServiceCatalog serviceCatalog;
    private final RestTemplate restTemplate;
    private final Duration cacheTtl;
    private final ConcurrentMap<String, CachedSpec> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ServiceHealth> health = new ConcurrentHashMap<>();

    public OpenApiSpecService(
            AggregatorProperties properties,
            ServiceCatalog serviceCatalog,
            RestTemplateBuilder restTemplateBuilder) {
        this.serviceCatalog = serviceCatalog;
        this.cacheTtl = properties.getCacheTtl();
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    public SpecResult getSpec(String serviceId) {
        ServiceDefinition service = serviceCatalog.getRequiredService(serviceId);
        CachedSpec cached = cache.get(serviceId);
        if (cached != null && !cached.isExpired(cacheTtl)) {
            return SpecResult.cached(service, cached, false, null);
        }
        return refreshSpec(service, cached, false);
    }

    public List<ServiceHealth> healthSnapshot() {
        synchronizeWithCatalog();
        List<ServiceHealth> snapshot = new ArrayList<>();
        for (ServiceDefinition service : serviceCatalog.getServices()) {
            CachedSpec cached = cache.get(service.getId());
            boolean stale = cached != null && cached.isExpired(cacheTtl);
            ServiceHealth entry = health.computeIfAbsent(service.getId(), id -> ServiceHealth.unknown(service));
            snapshot.add(entry.withStaleness(stale, cached));
        }
        return snapshot;
    }

    @Scheduled(fixedDelayString = "#{@aggregatorProperties.healthCheckIntervalMillis}", initialDelay = 5000)
    public void refreshAllServices() {
        synchronizeWithCatalog();
        for (ServiceDefinition service : serviceCatalog.getServices()) {
            SpecResult result = refreshSpec(service, cache.get(service.getId()), true);
            if (result == null) {
                LOGGER.debug("Heartbeat check failed for {}", service.getId());
            }
        }
    }

    private SpecResult refreshSpec(
            ServiceDefinition service,
            CachedSpec previous,
            boolean silent) {
        String serviceId = service.getId();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(service.getUrl(), String.class);
            MediaType contentType = resolveMediaType(response.getHeaders());
            CachedSpec updated = CachedSpec.success(
                    response.getBody(),
                    contentType,
                    response.getStatusCode(),
                    Instant.now());
            cache.put(serviceId, updated);
            ensureHealthEntry(serviceId, updated, ServiceStatus.UP, null);
            return SpecResult.fresh(service, updated);
        } catch (RestClientException ex) {
            String message = "Failed to fetch OpenAPI definition from " + service.getUrl();
            LOGGER.warn("{} - {}", message, ex.getMessage());
            if (previous != null) {
                ensureHealthEntry(serviceId, previous, ServiceStatus.DEGRADED, ex.getMessage());
                return SpecResult.cached(service, previous, true, ex.getMessage());
            }
            ensureHealthEntry(serviceId, null, ServiceStatus.DOWN, ex.getMessage());
            if (silent) {
                return null;
            }
            throw new UpstreamUnavailableException(service, ex);
        }
    }

    private void synchronizeWithCatalog() {
        Map<String, ServiceDefinition> current = new ConcurrentHashMap<>();
        for (ServiceDefinition service : serviceCatalog.getServices()) {
            current.put(service.getId(), service);
            health.computeIfAbsent(service.getId(), id -> ServiceHealth.unknown(service));
        }
        cache.keySet().removeIf(id -> !current.containsKey(id));
        health.keySet().removeIf(id -> !current.containsKey(id));
    }

    private void ensureHealthEntry(
            String serviceId,
            CachedSpec cached,
            ServiceStatus status,
            String message) {
        ServiceDefinition service = serviceCatalog.getRequiredService(serviceId);
        ServiceHealth current = health.get(serviceId);
        ServiceHealth updated = ServiceHealth.update(
                service,
                status,
                Instant.now(),
                cached,
                current != null ? current.getLastSuccess() : null,
                message);
        health.put(serviceId, updated);
        if (current == null || current.getStatus() != status) {
            LOGGER.info("Service '{}' status changed to {}", serviceId, status);
        }
    }

    private MediaType resolveMediaType(HttpHeaders headers) {
        MediaType mediaType = headers.getContentType();
        if (mediaType == null) {
            return MediaType.APPLICATION_JSON;
        }
        if (mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)
                || mediaType.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)) {
            return MediaType.APPLICATION_JSON;
        }
        if (mediaType.getSubtype() != null && mediaType.getSubtype().contains("yaml")) {
            return MediaType.valueOf("application/yaml");
        }
        return MediaType.APPLICATION_JSON;
    }
}
