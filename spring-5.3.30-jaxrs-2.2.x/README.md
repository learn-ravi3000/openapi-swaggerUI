# JAX-RS + Swagger Core Implementation Guide

Complete solution for adding Swagger/OpenAPI documentation to your JAX-RS application.
If you consume this service through the companion Swagger Aggregator UI (`swagger-aggregator-ui` module), that application proxies your OpenAPI document so browsers never call this app directly.
Keep the provided `CorsFilter` when exposing the API to browser clients on another origin; you can skip it when the aggregator is the only consumer.

## 📋 Files Included

| File | Location | Purpose |
|------|----------|---------|
| **pom.xml** | Root of project | Maven dependencies |
| **JaxRsApplication.java** | `src/main/java/com/example/api/config/` | JAX-RS application with OpenAPI config |
| **UserResource.java** | `src/main/java/com/example/api/resource/` | Example REST resource |
| **User.java** | `src/main/java/com/example/api/model/` | Example model |
| **SwaggerUIServlet.java** | `src/main/java/com/example/api/servlet/` | Serves Swagger UI |
| **CorsFilter.java** | `src/main/java/com/example/api/config/` | Adds `Access-Control-Allow-*` headers for direct browser access (optional behind aggregator) |
| **web.xml** | `src/main/webapp/WEB-INF/` | Web application configuration |
| **applicationContext.xml** | `src/main/resources/` | Spring configuration (optional) |

## 🚀 Implementation Steps

### Step 1: Update pom.xml

Replace or merge the provided `pom.xml` with your existing one. Key dependencies added:

```xml
<!-- Swagger Core for JAX-RS -->
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-jaxrs2</artifactId>
    <version>2.2.20</version>
</dependency>

<!-- Swagger UI -->
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>swagger-ui</artifactId>
    <version>5.10.3</version>
</dependency>
```

### Step 2: Create JAX-RS Application

Place `JaxRsApplication.java` in your config package. This file:
- Defines your API metadata (title, version, description)
- Registers your REST resources
- Registers the OpenAPI resource

**Important:** Update the following in JaxRsApplication.java:
- Change package name from `com.example.api` to your actual package
- Update API title, description, and version
- Update server URLs to match your deployment
- Add your resource classes to the `getClasses()` method

### Step 3: Add Swagger UI Servlet

Place `SwaggerUIServlet.java` in your servlet package. This servlet:
- Serves the Swagger UI interface
- Loads resources from the swagger-ui webjar
- Points to your OpenAPI specification

**No changes needed** - works as-is!

### Step 4: Configure web.xml

Merge the provided `web.xml` with your existing one. Add:

1. **Jersey Servlet Configuration:**
```xml
<servlet>
    <servlet-name>Jersey REST Service</servlet-name>
    <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
    <init-param>
        <param-name>javax.ws.rs.Application</param-name>
        <param-value>com.example.api.config.JaxRsApplication</param-value>
    </init-param>
    <init-param>
        <param-name>jersey.config.server.provider.packages</param-name>
        <param-value>com.example.api.resource,io.swagger.v3.jaxrs2.integration.resources</param-value>
    </init-param>
</servlet>
```

2. **Swagger UI Servlet Configuration:**
```xml
<servlet>
    <servlet-name>SwaggerUIServlet</servlet-name>
    <servlet-class>com.example.api.servlet.SwaggerUIServlet</servlet-class>
</servlet>
```

**Important:** Update package names to match your project!

### Step 5: Annotate Your REST Resources

Use `UserResource.java` as a template. Add Swagger annotations to your existing JAX-RS resources:

```java
@Path("/users")
@Tag(name = "Users", description = "User management operations")
public class UserResource {

    @GET
    @Path("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public Response getUserById(
        @Parameter(description = "User ID", required = true)
        @PathParam("id") Long id
    ) {
        // Your existing code
    }
}
```

### Step 6: Annotate Your Models

Use `User.java` as a template. Add schema annotations to your model classes:

```java
@Schema(description = "User account information")
public class User {
    
    @Schema(description = "Unique identifier", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "User's full name", example = "John Doe", required = true)
    private String name;
    
    // ... getters and setters
}
```

### Step 7: Build and Deploy

```bash
# Build the project
mvn clean package

# Deploy to Tomcat
# Copy target/jaxrs-swagger-demo-1.0.war to $TOMCAT_HOME/webapps/
```

## 🌐 Access Your Documentation

After deployment to Tomcat, access:

- **Swagger UI**: `http://localhost:8080/jaxrs-swagger-demo-1.0/swagger-ui/`
- **OpenAPI JSON**: `http://localhost:8080/jaxrs-swagger-demo-1.0/api/openapi.json`
- **OpenAPI YAML**: `http://localhost:8080/jaxrs-swagger-demo-1.0/api/openapi.yaml`
- **Your REST API**: `http://localhost:8080/jaxrs-swagger-demo-1.0/api/users`
## ?? Using This Service with the Swagger Aggregator UI

The `swagger-aggregator-ui` module proxies OpenAPI definitions so the browser only talks to `http://localhost:8081`. To surface this JAX-RS service inside the aggregator:

1. Deploy this WAR to Tomcat (default context path `jaxrs-swagger-demo-1.0`) on port `8082`.
2. In the aggregator's `application.yml`, add:
   ```yaml
   aggregator:
     services:
       - id: jaxrs-demo
         name: JAX-RS Demo
         url: http://localhost:8082/jaxrs-swagger-demo-1.0/api/openapi.json

   springdoc:
     swagger-ui:
       urls:
         - name: JAX-RS Demo
           url: /aggregated/jaxrs-demo
   ```
3. Restart the aggregator and browse `http://localhost:8081/swagger-ui/index.html`.

Because the aggregator fetches the OpenAPI document server-side, CORS headers from this application are only necessary when browsers call it directly. You can keep `CorsFilter` enabled for backwards compatibility or remove it for aggregator-only deployments.

## 📝 Key Swagger Annotations

### Class-Level Annotations

```java
@Tag(name = "Resource Name", description = "Description of this resource group")
```

### Method-Level Annotations

```java
@Operation(
    summary = "Short description",
    description = "Detailed description"
)

@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not Found")
})
```

### Parameter Annotations

```java
@Parameter(
    description = "Parameter description",
    required = true,
    example = "example-value"
)
```

### Model Field Annotations

```java
@Schema(
    description = "Field description",
    example = "example-value",
    required = true,
    minLength = 1,
    maxLength = 100
)
```

## 🎨 Customizing OpenAPI Definition

In `JaxRsApplication.java`, customize the `@OpenAPIDefinition`:

```java
@OpenAPIDefinition(
    info = @Info(
        title = "Your API Title",
        version = "1.0.0",
        description = "Detailed API description",
        contact = @Contact(
            name = "Your Team",
            email = "api@yourcompany.com",
            url = "https://yourcompany.com/support"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080/your-app", description = "Development"),
        @Server(url = "https://api.yourcompany.com", description = "Production")
    }
)
```

## 🔒 Adding Authentication Documentation

To document authentication:

```java
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class JaxRsApplication extends Application {
    // ...
}
```

Then on protected endpoints:

```java
@Operation(
    summary = "Protected endpoint",
    security = @SecurityRequirement(name = "bearerAuth")
)
```

## 🗂️ Recommended Project Structure

```
your-project/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/yourcompany/api/
        │       ├── config/
        │       │   └── JaxRsApplication.java
        │       ├── resource/
        │       │   ├── UserResource.java
       │       ├── model/
        │       │   ├── User.java
      │       └── servlet/
        │           └── SwaggerUIServlet.java
        ├── resources/
        │   └── applicationContext.xml
        └── webapp/
            └── WEB-INF/
                └── web.xml
```




