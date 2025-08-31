package rs.raf.rafeventbooker.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private static final Set<String> OPEN_PREFIXES = Set.of(
            "/public",
            "/auth/login"
    );

    private static final Set<String> PUBLIC_GET_PATHS = new HashSet<>(Arrays.asList(
            "/events",
            "/events/search",
            "/events/latest",
            "/events/create",
            "/events/most-viewed",
            "/events/most-reacted",
            "/events/by-category",
            "/events/by-tag",
            "/categories",
            "/tags"
    ));

    private static final String JWT_SECRET =
            Optional.ofNullable(System.getenv("JWT_SECRET")).orElse("CHANGE_ME_SUPER_SECRET_AND_LONG");
    private static final String JWT_ISSUER =
            Optional.ofNullable(System.getenv("JWT_ISSUER")).orElse("raf-event-booker");

    @Override
    public void filter(ContainerRequestContext requestContext) {

        final String method = requestContext.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        final String path = normalize(requestContext.getUriInfo().getPath());

        if (isOpen(path)) {
            return;
        }

        if ("GET".equalsIgnoreCase(method) && isPublicGet(path)) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abort(requestContext, Response.Status.UNAUTHORIZED, "Missing or invalid Authorization header.");
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        final DecodedJWT decoded;
        try {
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            decoded = JWT.require(algorithm)
                    .withIssuer(JWT_ISSUER)
                    .acceptLeeway(5)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            abort(requestContext, Response.Status.UNAUTHORIZED, "Invalid token.");
            return;
        }

        String role = decoded.getClaim("role").asString();
        if (role == null || role.isBlank()) {
            abort(requestContext, Response.Status.FORBIDDEN, "Role missing.");
            return;
        }

        Method resourceMethod = resourceInfo.getResourceMethod();
        if (resourceMethod != null && resourceMethod.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed ra = resourceMethod.getAnnotation(RolesAllowed.class);
            if (!Arrays.asList(ra.value()).contains(role)) {
                abort(requestContext, Response.Status.FORBIDDEN, "Insufficient role.");
            }
        }
    }

    private boolean isOpen(String path) {
        for (String p : OPEN_PREFIXES) {
            if (path.equals(p) || path.startsWith(p + "/")) return true;
        }
        return false;
    }

    private boolean isPublicGet(String path) {
        for (String p : PUBLIC_GET_PATHS) {
            if (path.equals(p) || path.startsWith(p + "/")) return true;
        }
        return false;
    }

    private void abort(ContainerRequestContext ctx, Response.Status status, String message) {
        ctx.abortWith(Response.status(status).entity(message).build());
    }

    private String normalize(String raw) {
        if (raw == null || raw.isEmpty()) return "/";
        String path = raw.startsWith("/") ? raw : ("/" + raw);
        if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }
}
