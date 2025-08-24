package rs.raf.rafeventbooker.config;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CORSResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        String origin = req.getHeaderString("Origin");
        if (origin == null) return;

        res.getHeaders().putSingle("Access-Control-Allow-Origin", origin); // ili tačno: http://localhost:5173
        res.getHeaders().putSingle("Vary", "Origin");
        res.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        // nije obavezno, ali korisno:
        res.getHeaders().putSingle("Access-Control-Expose-Headers", "Location");
    }
}
