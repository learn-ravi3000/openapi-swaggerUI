# Swagger Annotations Quick Reference

## Common Annotations Cheat Sheet

### On Resource Class

```java
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/users")
@Tag(name = "Users", description = "User management APIs")
public class UserResource {
    // ...
}
```

### On GET Method

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@GET
@Path("/{id}")
@Operation(summary = "Get user by ID")
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Success",
        content = @Content(schema = @Schema(implementation = User.class))
    ),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response getUser(@PathParam("id") Long id) {
    // ...
}
```

### On POST Method

```java
@POST
@Operation(summary = "Create user")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
})
public Response createUser(User user) {
    // ...
}
```

### On PUT Method

```java
@PUT
@Path("/{id}")
@Operation(summary = "Update user")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Updated"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response updateUser(@PathParam("id") Long id, User user) {
    // ...
}
```

### On DELETE Method

```java
@DELETE
@Path("/{id}")
@Operation(summary = "Delete user")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
public Response deleteUser(@PathParam("id") Long id) {
    // ...
}
```

### Path Parameters

```java
import io.swagger.v3.oas.annotations.Parameter;

@GET
@Path("/{id}")
public Response getUser(
    @Parameter(description = "User ID", required = true, example = "123")
    @PathParam("id") Long id
) {
    // ...
}
```

### Query Parameters

```java
@GET
public Response searchUsers(
    @Parameter(description = "Filter by name", example = "John")
    @QueryParam("name") String name,
    
    @Parameter(description = "Page number", example = "0")
    @QueryParam("page") @DefaultValue("0") int page
) {
    // ...
}
```

### Request Body

```java
@POST
public Response createUser(
    @Parameter(
        description = "User to create",
        required = true,
        schema = @Schema(implementation = User.class)
    )
    User user
) {
    // ...
}
```

### On Model Class

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User information")
public class User {
    
    @Schema(description = "User ID", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "User name", example = "John Doe", required = true)
    private String name;
    
    @Schema(description = "Email address", example = "john@example.com", required = true)
    private String email;
    
    // getters and setters
}
```

### Response with List

```java
@GET
@Operation(summary = "Get all users")
@ApiResponse(
    responseCode = "200",
    description = "List of users",
    content = @Content(
        schema = @Schema(implementation = User.class, type = "array")
    )
)
public Response getAllUsers() {
    // ...
}
```

### Hide Endpoint

```java
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@GET
@Path("/internal")
public Response internalEndpoint() {
    // This won't appear in Swagger UI
}
```

### Add Security

```java
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@GET
@Operation(
    summary = "Protected endpoint",
    security = @SecurityRequirement(name = "bearerAuth")
)
public Response protectedEndpoint() {
    // ...
}
```

## Required Imports

```java
// Core annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Optional annotations
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// JAX-RS annotations (your existing imports)
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
```

## Common HTTP Status Codes

```java
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "OK - Success"),
    @ApiResponse(responseCode = "201", description = "Created - Resource created"),
    @ApiResponse(responseCode = "204", description = "No Content - Success, no response body"),
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input"),
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
    @ApiResponse(responseCode = "403", description = "Forbidden - No permission"),
    @ApiResponse(responseCode = "404", description = "Not Found - Resource not found"),
    @ApiResponse(responseCode = "409", description = "Conflict - Resource already exists"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
```

## Schema Properties

```java
@Schema(
    description = "Field description",           // Describe the field
    example = "example-value",                   // Example value
    required = true,                             // Mark as required
    minLength = 1,                              // Minimum string length
    maxLength = 100,                            // Maximum string length
    minimum = "0",                              // Minimum number value
    maximum = "100",                            // Maximum number value
    pattern = "^[A-Za-z]+$",                   // Regex pattern
    format = "email",                           // Format (email, date, uuid, etc.)
    allowableValues = {"ACTIVE", "INACTIVE"},   // Enum values
    accessMode = Schema.AccessMode.READ_ONLY    // Read-only field
)
```

## Complete Example

```java
package com.example.api.resource;

import com.example.api.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
@Tag(name = "Users", description = "User management operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    @Operation(summary = "Get all users", description = "Returns a list of all users")
    @ApiResponse(responseCode = "200", description = "Success")
    public Response getAllUsers() {
        // Implementation
        return Response.ok().build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public Response getUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id") Long id
    ) {
        // Implementation
        return Response.ok().build();
    }

    @POST
    @Operation(summary = "Create new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public Response createUser(
        @Parameter(description = "User to create", required = true)
        User user
    ) {
        // Implementation
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public Response updateUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "Updated user data", required = true)
        User user
    ) {
        // Implementation
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public Response deleteUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id") Long id
    ) {
        // Implementation
        return Response.noContent().build();
    }
}
```
