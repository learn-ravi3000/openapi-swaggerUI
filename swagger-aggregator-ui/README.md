# API Documentation Portal

Centralized API documentation for all company services using OpenAPI and Swagger UI.

## Overview

This Spring Boot application aggregates OpenAPI specifications from multiple services running on different technologies:
- Spring Boot REST services
- Spring 5.3 + JAX-RS 2.2 services
- Jakarta EE 10 services (JBoss EAP 8.0)

All services are accessible through a single Swagger UI interface.

## Features

- ✅ Single URL for all API documentation
- ✅ Technology-agnostic (works with any OpenAPI-compliant service)
- ✅ Easy service addition via configuration
- ✅ Built-in Swagger UI
- ✅ No code changes required - pure configuration
- ✅ Environment-specific configuration support


## Project Structure
```
api-docs-aggregator/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/example
        │       ├── OpenApiAggregatorApplication.java
        └── resources/
            ├── application.yml
```

## Quick Start

### 1. Clone/Create the Project
```bash
mkdir api-docs-aggregator
cd api-docs-aggregator
```

### 2. Configure Your Services

Edit `src/main/resources/application.yml`:
```yaml
springdoc:
  swagger-ui:
    urls:
      - name: User Service
        url: http://your-user-service-host:8080/v3/api-docs
      - name: Order Service
        url: http://your-order-service-host:8081/openapi
      - name: Product Service
        url: http://your-product-service-host:8082/openapi
```

### 3. Build the Project
```bash
mvn clean install
```

### 4. Run the Application
```bash

mvn spring-boot:run
```

### 5. Access the Documentation

Open your browser and navigate to:
```
http://localhost:8081/swagger-ui/index.html
```

## Configuration

### Basic Configuration (application.yml)
```yaml
server:
  port: 8081

spring:
  application:
    name: api-documentation-portal

springdoc:
  swagger-ui:
    path: /docs                                    # Swagger UI path
    display-request-duration: true                 # Show request duration
    operations-sorter: alpha                       # Sort operations alphabetically
    tags-sorter: alpha                            # Sort tags alphabetically
    disable-swagger-default-url: true             # Disable default petstore example
    
    # Configure your services here
    urls:
      # Spring Boot Services (default endpoint: /v3/api-docs)
      - name: User Service (Spring Boot)
        url: http://user-service:8080/v3/api-docs
        
      # Spring 5.3 + JAX-RS Services (typical endpoint: /openapi or /openapi.json)
      - name: Order Service (Spring + JAX-RS)
        url: http://localhost:8082/jaxrs-swagger-demo-1.0/api/openapi.json
        
      # Jakarta EE 10 Services on JBoss EAP 8.0 (default endpoint: /openapi)
      - name: Product Service (Jakarta EE)
        url: http://product-service:8082/openapi
        
  # Disable local API docs generation (we're only aggregating)
  api-docs:
    enabled: false
```

### Step 1: Identify the OpenAPI Endpoint

Different technologies have different default endpoints:

| Technology | Default OpenAPI Endpoint | Configuration File |
|-----------|-------------------------|-------------------|
| **Spring Boot** | `/v3/api-docs` | `application.properties` |
| **Spring 5.3 + JAX-RS** | `/openapi` or `/openapi.json` | Configured in code |
| **Jakarta EE 10 (JBoss)** | `/openapi` | `microprofile-config.properties` |