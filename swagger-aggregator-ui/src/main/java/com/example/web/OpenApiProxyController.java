package com.example.web;

import com.example.service.OpenApiSpecService;
import com.example.service.model.SpecResult;
import com.example.service.model.UpstreamUnavailableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exposes aggregated OpenAPI documents to Swagger UI consumers via consistent IDs.
 */
@RestController
@RequestMapping("/aggregated")
public class OpenApiProxyController {

    private final OpenApiSpecService specService;

    public OpenApiProxyController(OpenApiSpecService specService) {
        this.specService = specService;
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<String> fetchOpenApi(@PathVariable String serviceId) {
        SpecResult result;
        try {
            result = specService.getSpec(serviceId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (UpstreamUnavailableException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Failed to fetch OpenAPI definition for '" + ex.getService().getId() + "'",
                    ex);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(result.getMediaType());
        headers.add("X-Aggregator-Cache", result.isFromCache() ? (result.isStale() ? "STALE" : "HIT") : "MISS");
        if (result.isStale()) {
            headers.add(
                    HttpHeaders.WARNING,
                    "110 swagger-aggregator \"Serving cached spec; upstream unreachable\"");
        }
        return ResponseEntity
                .status(result.getStatus())
                .headers(headers)
                .body(result.getBody());
    }
}
