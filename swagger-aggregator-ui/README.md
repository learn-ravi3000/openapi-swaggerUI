# API Documentation Portal

Centralized API documentation for all company services using OpenAPI and Swagger UI.
Service definitions now live in an external catalog, so the UI self-updates without
rebuilding the application.

## Features
- Config-driven service catalog loaded from YAML or JSON (`services.yml` by default)
- `/swagger-config.json` endpoint drives Swagger UI service discovery dynamically
- In-memory cache with short TTL keeps serving specs during upstream hiccups
- Heartbeat polling updates per-service health badges and exposes `/aggregated/health`
- Works with any OpenAPI-compliant service (Spring, JAX-RS, Jakarta EE, etc.)

## Project Structure
```
swagger-aggregator-ui/
├── pom.xml
├── README.md
├── src/main/
│   ├── java/com/example/
│   │   ├── OpenApiAggregatorApplication.java
│   │   ├── config/
│   │   │   ├── AggregatorProperties.java
│   │   │   └── ServiceCatalog.java
│   │   ├── service/
│   │   │   ├── OpenApiSpecService.java
│   │   │   └── model/*.java
│   │   └── web/
│   │       ├── OpenApiProxyController.java
│   │       └── SwaggerConfigController.java
│   └── resources/
│       ├── application.yml
│       └── services.yml
```

## Quick Start

1. **Build**
   ```bash
   mvn clean package
   ```
2. **Configure services**
   - Edit `src/main/resources/services.yml` (or place `config/services.yml` next to the jar).
   - Example:
     ```yaml
     services:
       - id: user-service
         name: User Service
         url: http://user-service.internal:8080/v3/api-docs
       - id: orders
         name: Order Service
         url: https://orders.company.com/openapi
     ```
3. **Tune aggregator settings** (optional) in `application.yml`:
   ```yaml
   aggregator:
     catalog-location: classpath:/services.yml   # or file:./config/services.yml
     cache-ttl: 30s                              # serve cached specs for 30 seconds
     health-check-interval: 30s                  # background heartbeat cadence
   springdoc:
     swagger-ui:
       config-url: /swagger-config.json
       disable-swagger-default-url: true
   ```
4. **Run**
   ```bash
   mvn spring-boot:run
   ```
5. **Browse**
   - Swagger UI: http://localhost:8081/swagger-ui/index.html
   - Swagger config feed: http://localhost:8081/swagger-config.json
   - Health snapshot: http://localhost:8081/aggregated/health

## Runtime Behaviour
- Swagger UI reads `/swagger-config.json`, which lists `urls` plus `health` metadata.
- `OpenApiSpecService` caches successful upstream responses for the configured TTL.
  When an upstream service is unavailable, the aggregator serves the last good copy
  and marks the service as `DEGRADED` in the health feed. If no cache exists, the
  proxy returns HTTP 503.
- A background heartbeat refreshes specs on a fixed delay so badges stay current.

## Adding or Updating Services
1. Add a new entry to `services.yml` (or the external catalog path).
2. Wait for the next heartbeat cycle or restart the application.
3. Reload Swagger UI; the new service appears automatically.

## Catalog File Format
- Root key `services` containing a list of objects with `id`, `name`, and `url`.
- JSON representation is also supported:
  ```json
  {
    "services": [
      {"id": "catalog", "name": "Catalog", "url": "https://catalog/v3/api-docs"}
    ]
  }
  ```
- Duplicate ids are rejected at startup.

## Health API Details
- `status`: `UP`, `DEGRADED`, `DOWN`, or `UNKNOWN`
- `stale`: true when the cached spec has expired but is still served
- `cachedStatus`: HTTP status code returned by the last successful fetch (if any)
- `lastChecked` / `lastSuccess`: timestamps used for heartbeat indicators

Use these feeds to render custom badges or alerts alongside Swagger UI.
