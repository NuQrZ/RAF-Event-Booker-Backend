package rs.raf.rafeventbooker.config;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 1)
public class CORSRequestFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        if ("OPTIONS".equalsIgnoreCase(containerRequestContext.getMethod())) {
            String origin = containerRequestContext.getHeaderString("Origin");
            String reqHeaders = containerRequestContext.getHeaderString("Access-Control-Request-Headers");

            Response.ResponseBuilder rb = Response.ok();
            if (origin != null) {
                rb.header("Access-Control-Allow-Origin", origin)
                        .header("Vary", "Origin")
                        .header("Access-Control-Allow-Credentials", "true")
                        .header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS")
                        .header("Access-Control-Allow-Headers", reqHeaders != null ? reqHeaders : "Authorization,Content-Type")
                        .header("Access-Control-Max-Age", "86400");
            }
            containerRequestContext.abortWith(rb.build());
        }
    }
}
