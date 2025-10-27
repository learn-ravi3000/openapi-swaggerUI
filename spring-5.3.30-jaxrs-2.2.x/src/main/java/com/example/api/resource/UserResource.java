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
import java.util.ArrayList;
import java.util.List;

@Path("/users")
@Tag(name = "User Management", description = "APIs for managing users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all users in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list",
            content = @Content(schema = @Schema(implementation = User.class))
        )
    })
    public Response getAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "John Doe", "john@example.com"));
        users.add(new User(2L, "Jane Smith", "jane@example.com"));
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their ID"
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
        @Parameter(description = "ID of user to retrieve", required = true)
        @PathParam("id") Long id
    ) {
        User user = new User(id, "John Doe", "john@example.com");
        return Response.ok(user).build();
    }

    @POST
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input"
        )
    })
    public Response createUser(
        @Parameter(description = "User object to create", required = true)
        User user
    ) {
        // In real app, save to database
        user.setId(3L);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(
        summary = "Update user",
        description = "Updates an existing user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public Response updateUser(
        @Parameter(description = "ID of user to update", required = true)
        @PathParam("id") Long id,
        @Parameter(description = "Updated user object", required = true)
        User user
    ) {
        user.setId(id);
        return Response.ok(user).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Deletes a user from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public Response deleteUser(
        @Parameter(description = "ID of user to delete", required = true)
        @PathParam("id") Long id
    ) {
        return Response.noContent().build();
    }
}
