package rs.raf.rafeventbooker.resources;

import rs.raf.rafeventbooker.model.Event;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.requests.rsvp.RsvpRequest;
import rs.raf.rafeventbooker.services.EventService;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/public/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicEventsResource {

    @Inject private EventService service;

    @GET
    public Page<Event> list(@QueryParam("page") @DefaultValue("1") int page,
                            @QueryParam("size") @DefaultValue("10") int size,
                            @QueryParam("query") String query) {
        return (query == null || query.isBlank())
                ? service.getEvents(page, size)
                : service.search(query, page, size);
    }

    @GET @Path("/latest")
    public List<Event> latest(@QueryParam("limit") @DefaultValue("10") int limit) {
        return service.getLatestEvents(limit);
    }

    @GET @Path("/most-viewed")
    public List<Event> mostViewed(@QueryParam("limit") @DefaultValue("10") int limit) {
        return service.mostViewedEventsLast30Days(limit);
    }

    @GET @Path("/most-reacted")
    public List<Event> mostReacted(@QueryParam("limit") @DefaultValue("3") int limit) {
        return service.mostReactedEvents(limit);
    }

    @GET @Path("/by-category/{categoryID}")
    public Page<Event> byCategory(@PathParam("categoryID") int categoryID,
                                  @QueryParam("page") @DefaultValue("1") int page,
                                  @QueryParam("size") @DefaultValue("10") int size) {
        return service.getEventsByCategory(categoryID, page, size);
    }

    @GET @Path("/by-tag/{tag}")
    public Page<Event> byTag(@PathParam("tag") String tag,
                             @QueryParam("page") @DefaultValue("1") int page,
                             @QueryParam("size") @DefaultValue("10") int size) {
        return service.getEventsByTag(tag, page, size);
    }

    @GET @Path("/{eventID}")
    public Response detail(@PathParam("eventID") int eventID,
                           @HeaderParam("X-Visitor-Id") String visitorID) {
        Event e = service.getEventByID(eventID)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        service.incrementViewsOnce(eventID, requireVisitor(visitorID));
        return Response.ok(e).build();
    }

    @POST @Path("/{eventID}/like")
    public Response like(@PathParam("eventID") int eventID,
                         @HeaderParam("X-Visitor-Id") String visitorID) {
        service.getEventByID(eventID).orElseThrow(() -> new NotFoundException("Event not found"));
        boolean ok = service.like(eventID, requireVisitor(visitorID));
        return ok ? Response.noContent().build() : Response.status(Response.Status.CONFLICT).build();
    }

    @POST @Path("/{eventID}/dislike")
    public Response dislike(@PathParam("eventID") int eventID,
                            @HeaderParam("X-Visitor-Id") String visitorID) {
        service.getEventByID(eventID).orElseThrow(() -> new NotFoundException("Event not found"));
        boolean ok = service.dislike(eventID, requireVisitor(visitorID));
        return ok ? Response.noContent().build() : Response.status(Response.Status.CONFLICT).build();
    }

    @POST @Path("/{eventID}/rsvp")
    public Response rsvp(@PathParam("eventID") int eventID, @Valid RsvpRequest body) {
        if (body == null || body.userEmail == null || body.userEmail.isBlank())
            throw new BadRequestException("userEmail required");
        boolean ok = service.rsvp(eventID, body.userEmail);
        return ok ? Response.noContent().build() : Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE @Path("/{eventID}/rsvp")
    public Response cancel(@PathParam("eventID") int eventID, @QueryParam("email") String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("email required");
        return service.cancelRsvp(eventID, email) ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET @Path("/{eventID}/rsvp-count")
    public Response rsvpCount(@PathParam("eventID") int eventID) {
        int count = service.countRSVPsByEvent(eventID);
        return Response.ok(count).build();
    }

    @GET @Path("/{eventID}/capacity")
    public Response capacity(@PathParam("eventID") int eventID) {
        int cap = service.capacityOfEvent(eventID);
        return Response.ok(cap).build();
    }

    @GET @Path("/{eventID}/similar")
    public List<Event> similar(@PathParam("eventID") int eventID,
                               @QueryParam("limit") @DefaultValue("3") int limit) {
        return service.similarEventsByTags(eventID, limit);
    }

    private String requireVisitor(String visitorID) {
        if (visitorID == null || visitorID.isBlank())
            throw new BadRequestException("Missing X-Visitor-Id header");
        return visitorID;
    }
}
