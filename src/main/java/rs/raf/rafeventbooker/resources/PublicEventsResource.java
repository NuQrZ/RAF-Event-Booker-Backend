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
@PermitAll
public class PublicEventsResource {

    @Inject private EventService eventService;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<Event> list(@QueryParam("page") @DefaultValue("1") int page,
                            @QueryParam("size") @DefaultValue("10") int size,
                            @QueryParam("query") String query) {
        return (query == null || query.isBlank())
                ? eventService.getEvents(page, size)
                : eventService.search(query, page, size);
    }

    @GET @Path("/latest")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Event> latest(@QueryParam("limit") @DefaultValue("10") int limit) {
        return eventService.getLatestEvents(limit);
    }

    @GET @Path("/most-viewed")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Event> mostViewed(@QueryParam("limit") @DefaultValue("10") int limit) {
        return eventService.mostViewedEventsLast30Days(limit);
    }

    @GET @Path("/most-reacted")
    public List<Event> mostReacted(@QueryParam("limit") @DefaultValue("3") int limit) {
        return eventService.mostReactedEvents(limit);
    }

    @GET @Path("/by-category/{categoryID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<Event> byCategory(@PathParam("categoryID") int categoryID,
                                  @QueryParam("page") @DefaultValue("1") int page,
                                  @QueryParam("size") @DefaultValue("10") int size) {
        return eventService.getEventsByCategory(categoryID, page, size);
    }

    @GET @Path("/by-tag/{tag}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Page<Event> byTag(@PathParam("tag") String tag,
                             @QueryParam("page") @DefaultValue("1") int page,
                             @QueryParam("size") @DefaultValue("10") int size) {
        return eventService.getEventsByTag(tag, page, size);
    }

    @GET @Path("/{eventID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response detail(@PathParam("eventID") int eventID,
                           @HeaderParam("VisitorId") String visitorID) {
        Event e = eventService.getEventByID(eventID)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (visitorID != null && !visitorID.isBlank()) {
            try { eventService.incrementViewsOnce(eventID, visitorID); }
            catch (Exception ignore) {}
        }

        return Response.ok(e).build();
    }

    @POST @Path("/{eventID}/like")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response like(@PathParam("eventID") int eventID,
                         @HeaderParam("VisitorId") String visitorID) {
        eventService.getEventByID(eventID).orElseThrow(() -> new NotFoundException("Event not found"));
        String v = requireVisitor(visitorID);
        boolean ok = eventService.like(eventID, v);
        return ok ? Response.noContent().build() : Response.status(Response.Status.CONFLICT).build();
    }

    @POST @Path("/{eventID}/dislike")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response dislike(@PathParam("eventID") int eventID,
                            @HeaderParam("VisitorId") String visitorID) {
        eventService.getEventByID(eventID).orElseThrow(() -> new NotFoundException("Event not found"));
        String v = requireVisitor(visitorID);
        boolean ok = eventService.dislike(eventID, v);
        return ok ? Response.noContent().build() : Response.status(Response.Status.CONFLICT).build();
    }

    @POST @Path("/{eventID}/rsvp")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rsvp(@PathParam("eventID") int eventID, @Valid RsvpRequest body) {
        if (body == null || body.userEmail == null || body.userEmail.isBlank())
            throw new BadRequestException("userEmail required");
        boolean ok = eventService.rsvp(eventID, body.userEmail);
        return ok ? Response.noContent().build() : Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE @Path("/{eventID}/rsvp")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancel(@PathParam("eventID") int eventID, @QueryParam("email") String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("email required");
        return eventService.cancelRsvp(eventID, email) ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET @Path("/{eventID}/rsvp-count")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rsvpCount(@PathParam("eventID") int eventID) {
        int count = eventService.countRSVPsByEvent(eventID);
        return Response.ok(count).build();
    }

    @GET @Path("/{eventID}/capacity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response capacity(@PathParam("eventID") int eventID) {
        int cap = eventService.capacityOfEvent(eventID);
        return Response.ok(cap).build();
    }

    @GET @Path("/{eventID}/similar")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Event> similar(@PathParam("eventID") int eventID,
                               @QueryParam("limit") @DefaultValue("3") int limit) {
        return eventService.similarEventsByTags(eventID, limit);
    }

    private String requireVisitor(String visitorID) {
        if (visitorID == null || visitorID.isBlank())
            throw new BadRequestException("Missing VisitorId header");
        return visitorID;
    }
}
