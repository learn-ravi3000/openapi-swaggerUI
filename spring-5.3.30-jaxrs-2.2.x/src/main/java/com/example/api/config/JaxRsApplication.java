package com.example.api.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "User Management API",
        version = "1.0.0",
        description = "REST API for managing users",
        contact = @Contact(
            name = "API Support",
            email = "support@example.com",
            url = "https://www.example.com/support"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            description = "Development Server",
            url = "http://localhost:8080/your-app-name"
        ),
        @Server(
            description = "Production Server",
            url = "https://api.example.com"
        )
    }
)
public class JaxRsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        
        // Add your REST resources
        resources.add(com.example.api.resource.UserResource.class);
        
        
        // Add OpenAPI resource for serving the OpenAPI spec
        resources.add(OpenApiResource.class);
        
        return resources;
    }
}
