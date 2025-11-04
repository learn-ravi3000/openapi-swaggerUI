package com.example.service.model;

import com.example.config.AggregatorProperties;
import org.springframework.web.client.RestClientException;

/**
 * Signals that an upstream OpenAPI document could not be fetched and no cache was available.
 */
public class UpstreamUnavailableException extends RuntimeException {

    private final AggregatorProperties.ServiceDefinition service;

    public UpstreamUnavailableException(
            AggregatorProperties.ServiceDefinition service,
            RestClientException cause) {
        super("Upstream service '" + service.getId() + "' unavailable", cause);
        this.service = service;
    }

    public AggregatorProperties.ServiceDefinition getService() {
        return service;
    }
}
