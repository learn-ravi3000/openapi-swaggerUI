package com.example.service.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

/**
 * Represents a cached upstream OpenAPI response with metadata needed for reuse decisions.
 */
public final class CachedSpec {

    private final String body;
    private final MediaType mediaType;
    private final HttpStatusCode status;
    private final Instant fetchedAt;

    private CachedSpec(
            String body,
            MediaType mediaType,
            HttpStatusCode status,
            Instant fetchedAt) {
        this.body = body;
        this.mediaType = mediaType;
        this.status = status;
        this.fetchedAt = fetchedAt;
    }

    public static CachedSpec success(
            String body,
            MediaType mediaType,
            HttpStatusCode status,
            Instant fetchedAt) {
        return new CachedSpec(body, mediaType, status, fetchedAt);
    }

    public boolean isExpired(Duration ttl) {
        if (fetchedAt == null || ttl == null) {
            return true;
        }
        return fetchedAt.plus(ttl).isBefore(Instant.now());
    }

    public String getBody() {
        return body;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    @Override
    public String toString() {
        return "CachedSpec{" +
                "status=" + status +
                ", mediaType=" + mediaType +
                ", fetchedAt=" + fetchedAt +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, mediaType, fetchedAt, body);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CachedSpec other)) {
            return false;
        }
        return Objects.equals(body, other.body)
                && Objects.equals(mediaType, other.mediaType)
                && Objects.equals(status, other.status)
                && Objects.equals(fetchedAt, other.fetchedAt);
    }
}
