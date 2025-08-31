package rs.raf.rafeventbooker.resources;

import rs.raf.rafeventbooker.requests.login.LoginRequest;
import rs.raf.rafeventbooker.services.AuthService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    private AuthService authService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
