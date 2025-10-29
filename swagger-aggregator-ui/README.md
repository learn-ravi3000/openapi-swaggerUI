# API Documentation Portal

Centralized API documentation for all company services using OpenAPI and Swagger UI.  
The UI gets every document through the Spring Boot proxy, so the browser always talks to `http://localhost:8081` and never hits backend services directly.

## Features
- Single URL for all API documentation
- Works with any OpenAPI-compliant service (Spring, JAX-RS, Jakarta EE, etc.)
- Pure configuration: add or remove services without touching Java code
- Built-in proxy keeps Swagger UI and OpenAPI responses on the same origin
- Powered by SpringDoc `springdoc-openapi-starter-webmvc-ui`

## Project Structure
```
swagger-aggregator-ui/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/example/
    │   ├── OpenApiAggregatorApplication.java
    │   ├── config/AggregatorProperties.java
    │   └── web/OpenApiProxyController.java
    └── resources/application.yml
```

## Quick Start

### 1. Build the project
```bash
mvn clean package
```

### 2. Configure your services

Edit `src/main/resources/application.yml`:
```yaml
server:
  port: 8081

aggregator:
  services:
    - id: user-service
      name: User Service
      url: http://user-service:8080/v3/api-docs
    - id: order-service
      name: Order Service
      url: http://orders:8081/openapi
    - id: product-service
      name: Product Service
      url: http://products:8082/openapi

springdoc:
  swagger-ui:
    disable-swagger-default-url: true
    urls:
      - name: User Service
        url: /aggregated/user-service
      - name: Order Service
        url: /aggregated/order-service
      - name: Product Service
        url: /aggregated/product-service
    urls-primary-name: User Service
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Browse the docs
Open your browser to `http://localhost:8081/swagger-ui/index.html`.  
Swagger UI loads each OpenAPI document via `/aggregated/{serviceId}`, so no CORS headers are required from the upstream services.

## Configuration Reference

```yaml
server:
  port: 8081

spring:
  application:
    name: api-documentation-portal

aggregator:
  services:
    - id: jaxrs-demo
      name: JAX-RS Demo
      url: http://localhost:8082/jaxrs-swagger-demo-1.0/api/openapi.json

springdoc:
  swagger-ui:
    path: /docs                                    # Optional: serve UI at /docs
    display-request-duration: true                 # Show request duration
    operations-sorter: alpha                       # Sort operations alphabetically
    tags-sorter: alpha                             # Sort tags alphabetically
    disable-swagger-default-url: true              # Remove petstore sample
    urls:
      - name: JAX-RS Demo
        url: /aggregated/jaxrs-demo                # Always point to /aggregated/{id}
    urls-primary-name: JAX-RS Demo
  api-docs:
    enabled: false                                 # Disable local API docs; we only aggregate
```

### Identifying OpenAPI endpoints

| Technology              | Default OpenAPI URL     | Notes / Configuration                           |
|-------------------------|-------------------------|--------------------------------------------------|
| Spring Boot             | `/v3/api-docs`          | Exposed by SpringDoc or springdoc-openapi       |
| Spring 5.3 + JAX-RS 2.2 | `/openapi` or `/openapi.json` | Provided by Swagger Core (see sample project) |
| Jakarta EE 10 (JBoss)   | `/openapi`              | Configure in `microprofile-config.properties`   |

### Adding a new service
1. Append a definition under `aggregator.services` with a unique `id`.
2. Add a corresponding entry under `springdoc.swagger-ui.urls` pointing to `/aggregated/{id}`.
3. Restart the Spring Boot application.

The proxy controller (`OpenApiProxyController`) fetches the upstream document with `RestTemplate`, preserves HTTP status, and returns JSON or YAML back to the browser while keeping everything on the same origin.
