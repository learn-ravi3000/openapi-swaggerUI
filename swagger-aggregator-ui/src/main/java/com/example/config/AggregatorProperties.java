package com.example.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {

    @Valid
    private List<ServiceDefinition> services = new ArrayList<>();

    public List<ServiceDefinition> getServices() {
        return services;
    }

    public void setServices(List<ServiceDefinition> services) {
        this.services = services;
    }

    public Optional<ServiceDefinition> findById(String id) {
        return services.stream().filter(service -> service.getId().equals(id)).findFirst();
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
