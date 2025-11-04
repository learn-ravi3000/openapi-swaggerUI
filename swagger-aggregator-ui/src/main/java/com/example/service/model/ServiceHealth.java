package com.example.service.model;

import com.example.config.AggregatorProperties;
import java.time.Instant;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

/**
 * Captures heartbeat state and cached response details for a single upstream service.
 */
public final class ServiceHealth {

    private final String serviceId;
    private final String serviceName;
    private final ServiceStatus status;
    private final Instant lastChecked;
    private final Instant lastSuccess;
    private final boolean stale;
    private final String message;
    private final Instant cachedAt;
    private final HttpStatusCode cachedStatus;
    private final MediaType cachedMediaType;

    private ServiceHealth(
            String serviceId,
            String serviceName,
            ServiceStatus status,
            Instant lastChecked,
            Instant lastSuccess,
            boolean stale,
            String message,
            Instant cachedAt,
            HttpStatusCode cachedStatus,
            MediaType cachedMediaType) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.status = status;
        this.lastChecked = lastChecked;
        this.lastSuccess = lastSuccess;
        this.stale = stale;
        this.message = message;
        this.cachedAt = cachedAt;
        this.cachedStatus = cachedStatus;
        this.cachedMediaType = cachedMediaType;
    }

    public static ServiceHealth unknown(AggregatorProperties.ServiceDefinition service) {
        return new ServiceHealth(
                service.getId(),
                service.getName(),
                ServiceStatus.UNKNOWN,
                null,
                null,
                false,
                null,
                null,
                null,
                null);
    }

    public static ServiceHealth update(
            AggregatorProperties.ServiceDefinition service,
            ServiceStatus status,
            Instant lastChecked,
            CachedSpec cached,
            Instant previousSuccess,
            String message) {
        Instant lastSuccess = cached != null ? cached.getFetchedAt() : previousSuccess;
        HttpStatusCode cachedStatus = cached != null ? cached.getStatus() : null;
        MediaType cachedType = cached != null ? cached.getMediaType() : null;
        return new ServiceHealth(
                service.getId(),
                service.getName(),
                status,
                lastChecked,
                lastSuccess,
                false,
                message,
                lastSuccess,
                cachedStatus,
                cachedType);
    }

    public ServiceHealth withStaleness(boolean stale, CachedSpec cached) {
        return new ServiceHealth(
                serviceId,
                serviceName,
                status,
                lastChecked,
                lastSuccess,
                stale,
                message,
                cached != null ? cached.getFetchedAt() : cachedAt,
                cached != null ? cached.getStatus() : cachedStatus,
                cached != null ? cached.getMediaType() : cachedMediaType);
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public Instant getLastChecked() {
        return lastChecked;
    }

    public Instant getLastSuccess() {
        return lastSuccess;
    }

    public boolean isStale() {
        return stale;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCachedAt() {
        return cachedAt;
    }

    public HttpStatusCode getCachedStatus() {
        return cachedStatus;
    }

    public MediaType getCachedMediaType() {
        return cachedMediaType;
    }

    public ServiceHealth withMessage(String newMessage) {
        return new ServiceHealth(
                serviceId,
                serviceName,
                status,
                lastChecked,
                lastSuccess,
                stale,
                newMessage,
                cachedAt,
                cachedStatus,
                cachedMediaType);
    }

    @Override
    public String toString() {
        return "ServiceHealth{" +
                "serviceId='" + serviceId + '\'' +
                ", status=" + status +
                ", lastChecked=" + lastChecked +
                ", stale=" + stale +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceHealth that)) {
            return false;
        }
        return stale == that.stale
                && Objects.equals(serviceId, that.serviceId)
                && Objects.equals(serviceName, that.serviceName)
                && status == that.status
                && Objects.equals(lastChecked, that.lastChecked)
                && Objects.equals(lastSuccess, that.lastSuccess)
                && Objects.equals(message, that.message)
                && Objects.equals(cachedAt, that.cachedAt)
                && Objects.equals(cachedStatus, that.cachedStatus)
                && Objects.equals(cachedMediaType, that.cachedMediaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                serviceId,
                serviceName,
                status,
                lastChecked,
                lastSuccess,
                stale,
                message,
                cachedAt,
                cachedStatus,
                cachedMediaType);
    }
}
