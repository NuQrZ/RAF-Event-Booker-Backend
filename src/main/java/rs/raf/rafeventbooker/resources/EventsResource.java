package rs.raf.rafeventbooker.resources;

import rs.raf.rafeventbooker.model.Event;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.model.RSVP;
import rs.raf.rafeventbooker.requests.events.CreateEventRequest;
import rs.raf.rafeventbooker.requests.events.UpdateEventRequest;
import rs.raf.rafeventbooker.services.EventService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/ems/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN","CREATOR"})
public class EventsResource {

    @Inject private EventService service;

    @GET
    public Page<Event> listEvents(@QueryParam("page") @DefaultValue("1") int page,
                            @QueryParam("size") @DefaultValue("10") int size,
                            @QueryParam("query") String query) {
        return (query == null || query.isBlank())
                ? service.getEvents(page, size)
                : service.search(query, page, size);
    }

    @GET @Path("/{eventID}")
    public Response getEvent(@PathParam("eventID") int eventID) {
        Event e = service.getEventByID(eventID)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return Response.ok(e).build();
    }

    @GET @Path("/by-category/{categoryID}")
    public Page<Event> listEventsByCategory(@PathParam("categoryID") int categoryID,
                                  @QueryParam("page") @DefaultValue("1") int page,
                                  @QueryParam("size") @DefaultValue("10") int size) {
        return service.getEventsByCategory(categoryID, page, size);
    }

    @GET @Path("/by-tag/{tag}")
    public Page<Event> listEventsByTag(@PathParam("tag") String tag,
                             @QueryParam("page") @DefaultValue("1") int page,
                             @QueryParam("size") @DefaultValue("10") int size) {
        return service.getEventsByTag(tag, page, size);
    }

    @GET @Path("/latest")
    public List<Event> latestEvents(@QueryParam("limit") @DefaultValue("10") int limit) {
        return service.getLatestEvents(limit);
    }

    @GET @Path("/most-viewed")
    public List<Event> mostViewedEvents(@QueryParam("limit") @DefaultValue("10") int limit) {
        return service.mostViewedEventsLast30Days(limit);
    }

    @GET @Path("/most-reacted")
    public List<Event> mostReactedEvents(@QueryParam("limit") @DefaultValue("3") int limit) {
        return service.mostReactedEvents(limit);
    }

    @POST
    public Response createEvent(@Valid CreateEventRequest body, @Context UriInfo uri) {
        Event e = new Event();
        e.setEventName(body.getEventName());
        e.setEventDescription(body.getEventDescription());
        e.setStartTime(body.getStartAt());
        e.setEventLocation(body.getEventLocation());
        e.setCategoryID(body.getCategoryID());
        e.setMaxCapacity(body.getMaxCapacity());
        List<String> tags = body.getTags() == null ? List.of() : body.getTags();

        int newId = service.createEvent(e, tags);
        return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(newId)).build()).build();
    }

    @PUT @Path("/{eventID}")
    public Response updateEvent(@PathParam("eventID") int eventID, @Valid UpdateEventRequest body) {
        Event e = new Event();
        e.setEventID(eventID);
        e.setEventName(body.getEventName());
        e.setEventDescription(body.getEventDescription());
        if (body.getStartAt() != null) e.setStartTime(body.getStartAt());
        e.setEventLocation(body.getEventLocation());
        e.setCategoryID(body.getCategoryID());
        e.setMaxCapacity(body.getMaxCapacity());
        List<String> tags = body.getTags() == null ? List.of() : body.getTags();

        int updated = service.updateEvent(e, tags);
        if (updated == 1) {
            return Response.noContent().build();
        }
        throw new NotFoundException("Event not found");
    }

    @DELETE @Path("/{eventID}")
    public Response deleteEvent(@PathParam("eventID") int eventID) {
        return service.deleteEvent(eventID) ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT @Path("/{eventID}/start-at")
    public Response setEventStartAt(@PathParam("eventID") int eventID,
                               @QueryParam("at") String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank()) throw new BadRequestException("Query param 'at' is required");
        final LocalDateTime t;
        try { t = LocalDateTime.parse(isoDateTime); }
        catch (DateTimeParseException ex) { throw new BadRequestException("Invalid ISO-8601 datetime"); }
        return service.setStartAt(eventID, t) ? Response.noContent().build()
                : Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET @Path("/{eventID}/capacity")
    public Response capacityOfEvent(@PathParam("eventID") int eventID) {
        int cap = service.capacityOfEvent(eventID);
        return Response.ok(cap).build();
    }

    @POST @Path("/{eventID}/rsvps")
    public Response adminCreateRsvp(@PathParam("eventID") int eventID, RSVP body, @Context UriInfo uri) {
        if (body == null || body.getUserEmail() == null || body.getUserEmail().isBlank())
            throw new BadRequestException("userEmail required");
        RSVP rsvp = new RSVP(eventID, body.getUserEmail(), body.getCreatedAt() == null ? LocalDateTime.now() : body.getCreatedAt());
        int res = service.createRsvp(rsvp);
        if (res < 0) throw new BadRequestException("Failed to create RSVP");
        return Response.created(uri.getAbsolutePathBuilder().path("rsvps").path(body.getUserEmail()).build()).build();
    }

    @GET @Path("/{eventID}/rsvps/count")
    public Response countRsvps(@PathParam("eventID") int eventID) {
        int count = service.countRSVPsByEvent(eventID);
        return Response.ok(count).build();
    }

    @GET @Path("/{eventID}/rsvps/exists")
    public Response rsvpExists(@PathParam("eventID") int eventID, @QueryParam("email") String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("email required");
        boolean exists = service.rsvpExists(eventID, email);
        return Response.ok(exists).build();
    }

    @GET @Path("/{eventID}/rsvps")
    public Response getRsvp(@PathParam("eventID") int eventID, @QueryParam("email") String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("email required");
        RSVP r = service.getRSVP(eventID, email)
                .orElseThrow(() -> new NotFoundException("RSVP not found"));
        return Response.ok(r).build();
    }

    @DELETE @Path("/{eventID}/rsvps")
    public Response adminCancel(@PathParam("eventID") int eventID, @QueryParam("email") String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("email required");
        return service.cancelRsvp(eventID, email) ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
