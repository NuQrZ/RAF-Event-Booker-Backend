package rs.raf.rafeventbooker.resources;

import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.model.User;
import rs.raf.rafeventbooker.requests.users.CreateUserRequest;
import rs.raf.rafeventbooker.requests.users.UpdateUserRequest;
import rs.raf.rafeventbooker.requests.users.UpdateUserStatusRequest;
import rs.raf.rafeventbooker.services.UserService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

@Path("/ems/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsersResource {
    @Inject
    private UserService userService;

    @GET
    @Path("/{userID}")
    @RolesAllowed("ADMIN")
    public Response getUser(@PathParam("userID") int userId) {
        return userService.getUserByID(userId)
                .map(Response::ok)
                .orElseThrow(() -> new NotFoundException("User not found!"))
                .build();
    }

    @GET
    @RolesAllowed("ADMIN")
    public Page<User> getAllUsers(@QueryParam("page") @DefaultValue("0") int page,
                                  @QueryParam("size") @DefaultValue("20") int size) {
        return userService.getAllUsers(page, size);
    }

    @GET
    @Path("/by-email")
    @RolesAllowed("ADMIN")
    public Response byEmail(@QueryParam("email") String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return userService.getUserByEmail(email.trim())
                .map(Response::ok)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .build();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response createUser(@Valid CreateUserRequest createUserRequest,
                               @Context UriInfo uriInfo) {
        User user = new User();
        user.setEmail(createUserRequest.email());
        user.setFirstName(createUserRequest.firstName());
        user.setLastName(createUserRequest.lastName());
        user.setPassword(createUserRequest.password());
        user.setUserRole(createUserRequest.userRole());
        user.setUserStatus(createUserRequest.userStatus());

        int userId = userService.createUser(user);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(userId)).build();
        return Response.created(location).entity(Map.of("user_id", userId)).build();
    }

    @DELETE
    @Path("/{userID}")
    @RolesAllowed("ADMIN")
    public Response deleteUser(@PathParam("userID") int userId) {
        userService.deleteUser(userId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{userID}")
    @RolesAllowed("ADMIN")
    public Response updateUser(@PathParam("userID") int userId,
                               @Valid UpdateUserRequest updateUserRequest) {
        User user = new User();
        user.setUserID(userId);
        user.setEmail(updateUserRequest.email());
        user.setFirstName(updateUserRequest.firstName());
        user.setLastName(updateUserRequest.lastName());
        user.setPassword(updateUserRequest.password());
        user.setUserRole(updateUserRequest.userRole());
        user.setUserStatus(updateUserRequest.userStatus());

        userService.updateUser(user);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{userID}/status")
    @RolesAllowed("ADMIN")
    public Response updateUserStatus(@PathParam("userID") int userId,
                                     @Valid UpdateUserStatusRequest updateUserStatusRequest) {
        userService.updateStatus(userId, updateUserStatusRequest.userStatus());
        return Response.noContent().build();
    }
}
