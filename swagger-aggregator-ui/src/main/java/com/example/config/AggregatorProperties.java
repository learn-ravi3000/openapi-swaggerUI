package com.example.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds aggregator-specific configuration such as catalog source, cache duration, and inline services.
 */
@Validated
@ConfigurationProperties(prefix = "aggregator")
@Component
public class AggregatorProperties {

    @NotBlank
    private String catalogLocation = "classpath:/services.yml";

    @NotNull
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration cacheTtl = Duration.ofSeconds(30);

    @NotNull
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration healthCheckInterval = Duration.ofSeconds(30);

    @Valid
    private List<ServiceDefinition> services = new ArrayList<>();

    private boolean tryItOutEnabled = true;

    public String getCatalogLocation() {
        return catalogLocation;
    }

    public void setCatalogLocation(String catalogLocation) {
        this.catalogLocation = catalogLocation;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public long getHealthCheckIntervalMillis() {
        return healthCheckInterval.toMillis();
    }

    public void setHealthCheckInterval(Duration healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }

    public List<ServiceDefinition> getServices() {
        return services;
    }

    public void setServices(List<ServiceDefinition> services) {
        this.services = services;
    }

    public Optional<ServiceDefinition> findById(String id) {
        return services.stream().filter(service -> service.getId().equals(id)).findFirst();
    }

    public boolean isTryItOutEnabled() {
        return tryItOutEnabled;
    }

    public void setTryItOutEnabled(boolean tryItOutEnabled) {
        this.tryItOutEnabled = tryItOutEnabled;
    }

    public static class ServiceDefinition {
        @NotBlank
        private String id;

        @NotBlank
        private String name;

        @NotNull
        private URI url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public URI getUrl() {
            return url;
        }

        public void setUrl(URI url) {
            this.url = url;
        }
    }
}
