package com.example.service.model;

import com.example.config.AggregatorProperties;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

/**
 * Value object returned to controllers describing the resolved spec and cache provenance.
 */
public final class SpecResult {

    private final AggregatorProperties.ServiceDefinition service;
    private final CachedSpec cachedSpec;
    private final boolean stale;
    private final boolean fromCache;
    private final String warning;

    private SpecResult(
            AggregatorProperties.ServiceDefinition service,
            CachedSpec cachedSpec,
            boolean stale,
            boolean fromCache,
            String warning) {
        this.service = service;
        this.cachedSpec = cachedSpec;
        this.stale = stale;
        this.fromCache = fromCache;
        this.warning = warning;
    }

    public static SpecResult fresh(
            AggregatorProperties.ServiceDefinition service,
            CachedSpec cachedSpec) {
        return new SpecResult(service, cachedSpec, false, false, null);
    }

    public static SpecResult cached(
            AggregatorProperties.ServiceDefinition service,
            CachedSpec cachedSpec,
            boolean stale,
            String warning) {
        return new SpecResult(service, cachedSpec, stale, true, warning);
    }

    public AggregatorProperties.ServiceDefinition getService() {
        return service;
    }

    public CachedSpec getCachedSpec() {
        return cachedSpec;
    }

    public boolean isStale() {
        return stale;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public String getWarning() {
        return warning;
    }

    public HttpStatusCode getStatus() {
        return cachedSpec.getStatus();
    }

    public MediaType getMediaType() {
        return cachedSpec.getMediaType();
    }

    public String getBody() {
        return cachedSpec.getBody();
    }

    @Override
    public String toString() {
        return "SpecResult{" +
                "service=" + service.getId() +
                ", status=" + cachedSpec.getStatus() +
                ", stale=" + stale +
                ", fromCache=" + fromCache +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpecResult specResult)) {
            return false;
        }
        return stale == specResult.stale
                && fromCache == specResult.fromCache
                && Objects.equals(service, specResult.service)
                && Objects.equals(cachedSpec, specResult.cachedSpec)
                && Objects.equals(warning, specResult.warning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, cachedSpec, stale, fromCache, warning);
    }
}
