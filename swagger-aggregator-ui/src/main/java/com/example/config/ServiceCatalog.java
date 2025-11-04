package com.example.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * Loads and validates the service catalog from external YAML/JSON resources, exposing lookup helpers.
 */
@Component
public class ServiceCatalog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCatalog.class);

    private final AggregatorProperties properties;
    private final ResourceLoader resourceLoader;
    private final Validator validator;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile List<AggregatorProperties.ServiceDefinition> services = List.of();
    private final ConcurrentMap<String, AggregatorProperties.ServiceDefinition> lookup = new ConcurrentHashMap<>();

    public ServiceCatalog(
            AggregatorProperties properties,
            ResourceLoader resourceLoader,
            Validator validator) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.validator = validator;
        this.jsonMapper = new ObjectMapper().findAndRegisterModules();
        this.yamlMapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
    }

    @PostConstruct
    public void initialize() {
        reload();
    }

    public List<AggregatorProperties.ServiceDefinition> getServices() {
        lock.readLock().lock();
        try {
            return services;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<AggregatorProperties.ServiceDefinition> findById(String id) {
        return Optional.ofNullable(lookup.get(id));
    }

    public AggregatorProperties.ServiceDefinition getRequiredService(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown service '" + id + "'"));
    }

    public void reload() {
        lock.writeLock().lock();
        try {
            List<AggregatorProperties.ServiceDefinition> loaded = loadCatalog();
            services = Collections.unmodifiableList(loaded);
            lookup.clear();
            for (AggregatorProperties.ServiceDefinition service : services) {
                lookup.put(service.getId(), service);
            }
            LOGGER.info("Loaded {} service definitions for Swagger aggregation", services.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<AggregatorProperties.ServiceDefinition> loadCatalog() {
        List<AggregatorProperties.ServiceDefinition> definitions = loadExternalCatalog()
                .orElseGet(() -> new ArrayList<>(properties.getServices()));

        if (definitions.isEmpty()) {
            LOGGER.warn("Service catalog is empty; Swagger UI will not list any upstream services");
        }

        Map<String, AggregatorProperties.ServiceDefinition> deduplicated = new LinkedHashMap<>();
        for (AggregatorProperties.ServiceDefinition definition : definitions) {
            validateDefinition(definition);
            AggregatorProperties.ServiceDefinition previous = deduplicated.putIfAbsent(definition.getId(), definition);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate service id '" + definition.getId() + "' in catalog definitions");
            }
        }
        return List.copyOf(deduplicated.values());
    }

    private Optional<List<AggregatorProperties.ServiceDefinition>> loadExternalCatalog() {
        Resource resource = resourceLoader.getResource(properties.getCatalogLocation());
        if (!resource.exists()) {
            LOGGER.info("Catalog resource '{}' not found; falling back to inline configuration",
                    properties.getCatalogLocation());
            return Optional.empty();
        }

        try {
            byte[] raw = readResource(resource);
            if (raw.length == 0) {
                LOGGER.warn("Catalog resource '{}' is empty", properties.getCatalogLocation());
                return Optional.of(List.of());
            }
            List<AggregatorProperties.ServiceDefinition> parsed = parseCatalog(raw, resource);
            return Optional.of(parsed);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Failed to load service catalog from " + properties.getCatalogLocation(), ex);
        }
    }

    private byte[] readResource(Resource resource) throws IOException {
        try (InputStream input = resource.getInputStream()) {
            return FileCopyUtils.copyToByteArray(input);
        }
    }

    private List<AggregatorProperties.ServiceDefinition> parseCatalog(
            byte[] raw,
            Resource resource) throws IOException {
        ObjectMapper primaryMapper = selectMapper(raw, resource);
        try {
            List<AggregatorProperties.ServiceDefinition> parsed = tryParse(raw, primaryMapper);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        } catch (IOException ex) {
            LOGGER.debug("Failed to parse catalog with {} reader: {}", mapperName(primaryMapper), ex.getMessage());
        }

        ObjectMapper fallbackMapper = primaryMapper == yamlMapper ? jsonMapper : yamlMapper;
        try {
            return tryParse(raw, fallbackMapper);
        } catch (IOException ex) {
            LOGGER.error("Unable to parse service catalog using {} or {}",
                    mapperName(primaryMapper),
                    mapperName(fallbackMapper));
            throw ex;
        }
    }

    private ObjectMapper selectMapper(byte[] raw, Resource resource) {
        String filename = resource.getFilename();
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".json")) {
                return jsonMapper;
            }
            if (lower.endsWith(".yml") || lower.endsWith(".yaml")) {
                return yamlMapper;
            }
        }
        for (byte b : raw) {
            if (Character.isWhitespace(b)) {
                continue;
            }
            if (b == '{' || b == '[') {
                return jsonMapper;
            }
            break;
        }
        return yamlMapper;
    }

    private String mapperName(ObjectMapper mapper) {
        return mapper == jsonMapper ? "JSON" : "YAML";
    }

    private List<AggregatorProperties.ServiceDefinition> tryParse(
            byte[] raw,
            ObjectMapper mapper) throws IOException {
        CatalogDocument document = mapper.readValue(raw, CatalogDocument.class);
        if (document != null && document.getServices() != null && !document.getServices().isEmpty()) {
            return document.getServices();
        }
        List<AggregatorProperties.ServiceDefinition> direct = mapper.readValue(
                raw,
                new TypeReference<List<AggregatorProperties.ServiceDefinition>>() {
                });
        return direct == null ? List.of() : direct;
    }

    private void validateDefinition(AggregatorProperties.ServiceDefinition definition) {
        Set<ConstraintViolation<AggregatorProperties.ServiceDefinition>> violations = validator.validate(definition);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private static class CatalogDocument {
        @Valid
        private List<AggregatorProperties.ServiceDefinition> services = new ArrayList<>();

        public List<AggregatorProperties.ServiceDefinition> getServices() {
            return services;
        }

        public void setServices(List<AggregatorProperties.ServiceDefinition> services) {
            this.services = services;
        }
    }
}
