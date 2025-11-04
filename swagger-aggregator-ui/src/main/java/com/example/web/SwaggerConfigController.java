package com.example.web;

import com.example.config.AggregatorProperties;
import com.example.config.ServiceCatalog;
import com.example.service.OpenApiSpecService;
import com.example.service.model.ServiceHealth;
import com.example.service.model.ServiceStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes dynamic Swagger UI configuration and health data derived from the service catalog.
 */
@RestController
public class SwaggerConfigController {

    private final ServiceCatalog serviceCatalog;
    private final OpenApiSpecService specService;
    private final AggregatorProperties properties;

    public SwaggerConfigController(
            ServiceCatalog serviceCatalog,
            OpenApiSpecService specService,
            AggregatorProperties properties) {
        this.serviceCatalog = serviceCatalog;
        this.specService = specService;
        this.properties = properties;
    }

    @GetMapping(value = "/swagger-config.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public SwaggerConfigResponse swaggerConfig() {
        List<AggregatorProperties.ServiceDefinition> services = serviceCatalog.getServices();
        List<ServiceEntry> urls = new ArrayList<>();
        Map<String, ServiceHealth> healthById = specService.healthSnapshot().stream()
                .collect(Collectors.toMap(
                        ServiceHealth::getServiceId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));

        Map<String, HealthEntry> health = new LinkedHashMap<>();
        for (AggregatorProperties.ServiceDefinition service : services) {
            urls.add(new ServiceEntry(
                    service.getName(),
                    "/aggregated/" + service.getId(),
                    service.getId()));
            ServiceHealth snapshot = healthById.get(service.getId());
            if (snapshot != null) {
                health.put(service.getId(), HealthEntry.from(snapshot));
            } else {
                health.put(service.getId(), HealthEntry.from(ServiceHealth.unknown(service)));
            }
        }

        String primaryName = urls.isEmpty() ? null : urls.get(0).name();
        return new SwaggerConfigResponse(
                urls, primaryName, properties.getCacheTtl(), health, properties.isTryItOutEnabled());
    }

    @GetMapping(value = "/aggregated/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, HealthEntry> serviceHealth() {
        return specService.healthSnapshot().stream()
                .map(HealthEntry::from)
                .collect(Collectors.toMap(
                        HealthEntry::id,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    public record SwaggerConfigResponse(
            List<ServiceEntry> urls,
            String urlsPrimaryName,
            Duration cacheTtl,
            Map<String, HealthEntry> health,
            boolean tryItOutEnabled) {
    }

    public record ServiceEntry(
            String name,
            String url,
            String id) {
    }

    public record HealthEntry(
            String id,
            String name,
            ServiceStatus status,
            boolean stale,
            String message,
            String cachedContentType,
            String cachedStatus,
            java.time.Instant lastChecked,
            java.time.Instant lastSuccess) {

        private static HealthEntry from(ServiceHealth health) {
            String contentType = health.getCachedMediaType() != null
                    ? health.getCachedMediaType().toString()
                    : null;
            String cachedStatus = health.getCachedStatus() != null
                    ? health.getCachedStatus().toString()
                    : null;
            return new HealthEntry(
                    health.getServiceId(),
                    health.getServiceName(),
                    health.getStatus(),
                    health.isStale(),
                    health.getMessage(),
                    contentType,
                    cachedStatus,
                    health.getLastChecked(),
                    health.getLastSuccess());
        }
    }
}
