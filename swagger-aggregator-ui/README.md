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

## Prerequisites

- JDK 17 or higher
- Maven 3.6+
- Access to all backend services

## Project Structure
```
api-docs-aggregator/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/company/apidocs/
        │       ├── ApiDocsAggregatorApplication.java
        │       └── config/
        │           └── WebConfig.java (optional)
        └── resources/
            ├── application.yml
            ├── application-dev.yml (optional)
            └── application-prod.yml (optional)
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
mvn clean package
```

### 4. Run the Application
```bash
java -jar target/api-docs-aggregator-1.0.0.jar
```

### 5. Access the Documentation

Open your browser and navigate to:
```
http://localhost:8080/docs
```

## Configuration

### Basic Configuration (application.yml)
```yaml
server:
  port: 8080

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
        url: http://order-service:8081/openapi
        
      # Jakarta EE 10 Services on JBoss EAP 8.0 (default endpoint: /openapi)
      - name: Product Service (Jakarta EE)
        url: http://product-service:8082/openapi
        
      # Add more services as needed
      - name: Shipping Service
        url: http://shipping-service:8083/v3/api-docs
        
      - name: Inventory Service
        url: http://inventory-service:8084/openapi
  
  # Disable local API docs generation (we're only aggregating)
  api-docs:
    enabled: false
```

### Environment-Specific Configuration

#### Development (application-dev.yml)
```yaml
springdoc:
  swagger-ui:
    urls:
      - name: User Service
        url: http://localhost:8081/v3/api-docs
      - name: Order Service
        url: http://localhost:8082/openapi
      - name: Product Service
        url: http://localhost:8083/openapi
```

#### Production (application-prod.yml)
```yaml
springdoc:
  swagger-ui:
    urls:
      - name: User Service
        url: https://user-service.company.com/v3/api-docs
      - name: Order Service
        url: https://order-service.company.com/openapi
      - name: Product Service
        url: https://product-service.company.com/openapi
```

**Run with specific profile:**
```bash
# Development
java -jar target/api-docs-aggregator-1.0.0.jar --spring.profiles.active=dev

# Production
java -jar target/api-docs-aggregator-1.0.0.jar --spring.profiles.active=prod
```

### Using Environment Variables
```yaml
springdoc:
  swagger-ui:
    urls:
      - name: User Service
        url: ${USER_SERVICE_URL:http://localhost:8081/v3/api-docs}
      - name: Order Service
        url: ${ORDER_SERVICE_URL:http://localhost:8082/openapi}
      - name: Product Service
        url: ${PRODUCT_SERVICE_URL:http://localhost:8083/openapi}
```

**Run with environment variables:**
```bash
export USER_SERVICE_URL=http://user-service:8080/v3/api-docs
export ORDER_SERVICE_URL=http://order-service:8081/openapi
export PRODUCT_SERVICE_URL=http://product-service:8082/openapi

java -jar target/api-docs-aggregator-1.0.0.jar
```

## Adding New Services

### Step 1: Identify the OpenAPI Endpoint

Different technologies have different default endpoints:

| Technology | Default OpenAPI Endpoint | Configuration File |
|-----------|-------------------------|-------------------|
| **Spring Boot** | `/v3/api-docs` | `application.properties` |
| **Spring 5.3 + JAX-RS** | `/openapi` or `/openapi.json` | Configured in code |
| **Jakarta EE 10 (JBoss)** | `/openapi` | `microprofile-config.properties` |

### Step 2: Add to Configuration

Add the service to `application.yml`:
```yaml
springdoc:
  swagger-ui:
    urls:
      - name: New Service Name
        url: http://new-service-host:port/openapi-endpoint
```

### Step 3: Restart the Aggregator
```bash
# Stop the application (Ctrl+C)
# Restart
java -jar target/api-docs-aggregator-1.0.0.jar
```

The new service will appear in the Swagger UI dropdown automatically.

## Verifying Service Endpoints

Before adding a service to the aggregator, verify its OpenAPI endpoint is accessible:
```bash
# For Spring Boot services
curl http://user-service:8080/v3/api-docs

# For JAX-RS services
curl http://order-service:8081/openapi

# For Jakarta EE services
curl http://product-service:8082/openapi
```

You should receive a JSON response containing the OpenAPI specification.

## Deployment

### Local/Dev Server Deployment
```bash
# Build
mvn clean package

# Copy JAR to server
scp target/api-docs-aggregator-1.0.0.jar user@server:/opt/api-docs/

# Run on server
ssh user@server
cd /opt/api-docs
nohup java -jar api-docs-aggregator-1.0.0.jar --spring.profiles.active=prod > logs/app.log 2>&1 &
```

### Windows Server Deployment
```bash
# Build
mvn clean package

# Copy JAR to server
copy target\api-docs-aggregator-1.0.0.jar \\server\api-docs\

# Run as Windows Service (using NSSM or similar)
# Or run directly:
java -jar api-docs-aggregator-1.0.0.jar --spring.profiles.active=prod
```

### JBoss/WildFly Deployment (WAR)

To deploy on JBoss, convert to WAR:

**pom.xml changes:**
```xml
<packaging>war</packaging>

<dependencies>
    <!-- Add servlet dependency -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Extend SpringBootServletInitializer:**
```java
@SpringBootApplication
public class ApiDocsAggregatorApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApiDocsAggregatorApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiDocsAggregatorApplication.class, args);
    }
}
```

**Build and deploy:**
```bash
mvn clean package
cp target/api-docs-aggregator-1.0.0.war $JBOSS_HOME/standalone/deployments/
```

### Running as a System Service (Linux)

Create systemd service file: `/etc/systemd/system/api-docs.service`
```ini
[Unit]
Description=API Documentation Portal
After=network.target

[Service]
Type=simple
User=apidocs
ExecStart=/usr/bin/java -jar /opt/api-docs/api-docs-aggregator-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=api-docs

[Install]
WantedBy=multi-user.target
```

**Enable and start:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable api-docs
sudo systemctl start api-docs
sudo systemctl status api-docs
```

## Troubleshooting

### Issue: Services Not Appearing in Dropdown

**Solution:**
1. Verify service URLs are accessible from the aggregator server
2. Check OpenAPI endpoints return valid JSON:
```bash
   curl -v http://service-host:port/openapi-endpoint
```
3. Check for CORS issues (see CORS Configuration below)
4. Review application logs for errors

### Issue: "Failed to Fetch" Error in Swagger UI

**Causes:**
- Service is down or unreachable
- Network/firewall blocking access
- Incorrect URL configuration
- CORS issues

**Solutions:**
```yaml
# Increase timeout
springdoc:
  swagger-ui:
    url-fetch-timeout: 10000  # 10 seconds (default is 5000)
```

### Issue: CORS Errors

If services are on different domains, add CORS configuration:

**WebConfig.java:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

### Issue: Port Already in Use

Change the port in `application.yml`:
```yaml
server:
  port: 9090  # Use different port
```

## Health Check

Access application health:
```bash
curl http://localhost:8080/actuator/health
```

To enable actuator, add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

And configure in `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

## Monitoring & Logs

### View Logs
```bash
# If running in foreground
# Logs appear in console

# If running with nohup
tail -f nohup.out

# If running as systemd service
sudo journalctl -u api-docs -f

# Configure custom log file
java -jar api-docs-aggregator-1.0.0.jar > /var/log/api-docs/app.log 2>&1
```

### Configure Logging

Add to `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.company.apidocs: DEBUG
    org.springframework.web: INFO
  file:
    name: /var/log/api-docs/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Security

### Basic Authentication

Add Spring Security dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Configure in `application.yml`:
```yaml
spring:
  security:
    user:
      name: admin
      password: changeme
```

### Custom Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/docs/**", "/v3/**", "/swagger-ui/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic();
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("api-viewer")
            .password("{noop}SecurePassword123")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```

## Upgrading

### Upgrade SpringDoc Version

1. Update version in `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.4.0</version> <!-- Update to latest -->
</dependency>
```

2. Rebuild:
```bash
mvn clean package
```

3. Redeploy

### Upgrade Spring Boot Version

1. Update parent version in `pom.xml`:
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version> <!-- Update to latest -->
</parent>
```

2. Check for breaking changes in Spring Boot release notes
3. Rebuild and test

## Performance Tuning

### JVM Options
```bash
java -Xms512m -Xmx1024m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar api-docs-aggregator-1.0.0.jar
```

### Connection Pooling

Add to `application.yml`:
```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

## Best Practices

1. **Use Environment Variables** for service URLs in production
2. **Enable Health Checks** to monitor service availability
3. **Implement Caching** if services have slow OpenAPI endpoints
4. **Use HTTPS** in production for all service URLs
5. **Set Appropriate Timeouts** based on network conditions
6. **Monitor Logs** regularly for errors
7. **Document Service Naming** conventions in configuration
8. **Version Control** the configuration files

## Support & Maintenance

### Common Maintenance Tasks

**Adding a New Service:**
1. Add service URL to `application.yml`
2. Restart application
3. Verify service appears in Swagger UI

**Removing a Service:**
1. Remove or comment out service URL in `application.yml`
2. Restart application

**Updating Service URL:**
1. Update URL in `application.yml`
2. Restart application

### Getting Help

For issues or questions:
1. Check this README
2. Review application logs
3. Verify service OpenAPI endpoints are accessible
4. Contact API Platform Team: api-team@company.com

## License

Copyright © 2024 Company Name. All rights reserved.

## Changelog

### Version 1.0.0 (Initial Release)
- Basic aggregation functionality
- Support for Spring Boot, Spring 5.3 + JAX-RS, Jakarta EE 10
- Swagger UI integration
- Environment-specific configuration
- Health check endpoint