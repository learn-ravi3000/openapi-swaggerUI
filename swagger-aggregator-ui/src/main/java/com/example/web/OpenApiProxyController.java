package com.example.web;

import com.example.config.AggregatorProperties;
import com.example.config.AggregatorProperties.ServiceDefinition;
import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/aggregated")
public class OpenApiProxyController {

    private final AggregatorProperties properties;
    private final RestTemplate restTemplate;

    public OpenApiProxyController(
            AggregatorProperties properties,
            RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    @GetMapping(value = "/{serviceId}")
    public ResponseEntity<String> fetchOpenApi(@PathVariable String serviceId) {
        ServiceDefinition service = locateService(serviceId);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(service.getUrl(), String.class);
            MediaType contentType = Optional.ofNullable(response.getHeaders().getContentType())
                    .filter(mediaType -> mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)
                            || mediaType.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                            || mediaType.getSubtype().contains("yaml"))
                    .orElse(MediaType.APPLICATION_JSON);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to fetch OpenAPI definition from " + service.getUrl(),
                    ex);
        }
    }

    private ServiceDefinition locateService(String serviceId) {
        return properties.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown service '" + serviceId + "'"));
    }
}
