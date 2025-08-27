package rs.raf.rafeventbooker.services;

import rs.raf.rafeventbooker.model.Event;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.model.RSVP;
import rs.raf.rafeventbooker.repositories.events.EventsRepository;
import rs.raf.rafeventbooker.repositories.rsvp.RsvpRepository;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventService {

    @Inject private EventsRepository eventsRepository;
    @Inject private RsvpRepository rsvpRepository;

    public Optional<Event> getEventByID(int eventID) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        return eventsRepository.getEventByID(eventID);
    }

    public Page<Event> getEvents(int page, int size) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : Math.min(size, 100);
        return eventsRepository.list(p, s);
    }

    public Page<Event> search(String text, int page, int size) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : Math.min(size, 100);
        return eventsRepository.search(text, p, s);
    }

    public Page<Event> getEventsByCategory(int categoryID, int page, int size) {
        if (categoryID <= 0) {
            throw new BadRequestException("Invalid categoryID");
        }
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : Math.min(size, 100);
        return eventsRepository.listByCategory(categoryID, p, s);
    }

    public Page<Event> getEventsByTag(String tagName, int page, int size) {
        String t = normalize(tagName);
        if (t == null || t.isBlank()) {
            throw new BadRequestException("tagName required");
        }
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : Math.min(size, 100);
        return eventsRepository.listByTag(t, p, s);
    }

    public List<Event> getLatestEvents(int limit) {
        int lim = limit <= 0 ? 10 : Math.min(limit, 100);
        return eventsRepository.latest(lim);
    }

    public List<Event> mostViewedEventsLast30Days(int limit) {
        int lim = limit <= 0 ? 10 : Math.min(limit, 100);
        return eventsRepository.mostViewedLast30Days(lim);
    }

    public List<Event> mostReactedEvents(int limit) {
        int lim = limit <= 0 ? 3 : Math.min(limit, 100);
        return eventsRepository.mostReacted(lim);
    }

    public List<Event> similarEventsByTags(int eventID, int limit) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        int lim = limit <= 0 ? 3 : Math.min(limit, 50);
        return eventsRepository.similarByTags(eventID, lim);
    }

    public int createEvent(Event event, List<String> tags) {
        validateEventForCreateOrUpdate(event);
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        return eventsRepository.create(event, normalizeTags(tags));
    }

    public int updateEvent(Event event, List<String> tags) {
        validateEventForCreateOrUpdate(event);
        return eventsRepository.update(event, normalizeTags(tags));
    }

    public boolean deleteEvent(int eventID) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        return eventsRepository.delete(eventID);
    }

    public boolean incrementViewsOnce(int eventID, String visitorID) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        return eventsRepository.incrementViewsOnce(eventID, requireVisitor(visitorID));
    }

    public boolean like(int eventID, String visitorID) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        return eventsRepository.like(eventID, requireVisitor(visitorID));
    }

    public boolean dislike(int eventID, String visitorID) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        return eventsRepository.dislike(eventID, requireVisitor(visitorID));
    }

    public int createRsvp(RSVP rsvp) {
        if (rsvp == null || rsvp.getEventID() <= 0 || rsvp.getUserEmail() == null || rsvp.getUserEmail().isBlank())
            throw new BadRequestException("Invalid RSVP");
        if (rsvp.getCreatedAt() == null) {
            rsvp.setCreatedAt(LocalDateTime.now());
        }
        return rsvpRepository.createRsvp(rsvp);
    }

    public int countRSVPsByEvent(int eventID) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        return rsvpRepository.countRSVPsByEvent(eventID);
    }

    public boolean rsvpExists(int eventID, String userEmail) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("userEmail required");
        }
        return rsvpRepository.rsvpExists(eventID, userEmail);
    }

    public Optional<RSVP> getRSVP(int eventID, String userEmail) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("userEmail required");
        }
        return rsvpRepository.getRSVP(eventID, userEmail);
    }

    public boolean rsvp(int eventID, String userEmail) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("userEmail required");
        }

        eventsRepository.getEventByID(eventID)
                .orElseThrow(() -> new BadRequestException("Event not found"));

        int capacity = eventsRepository.capacityOf(eventID);
        LocalDateTime now = LocalDateTime.now();

        if (capacity == 0) {
            int ins = rsvpRepository.createRsvp(new RSVP(eventID, userEmail, now));
            return ins == 1 || ins == 0;
        }

        int ins = rsvpRepository.createRsvp(new RSVP(eventID, userEmail, now));
        if (ins == 0) {
            return true;
        }

        int current = rsvpRepository.countRSVPsByEvent(eventID);
        if (current > capacity) {
            rsvpRepository.deleteRsvp(eventID, userEmail);
            throw new BadRequestException("Capacity reached");
        }
        return true;
    }

    public boolean cancelRsvp(int eventID, String userEmail) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("userEmail required");
        }
        return rsvpRepository.deleteRsvp(eventID, userEmail);
    }

    public int capacityOfEvent(int eventID) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        return eventsRepository.capacityOf(eventID);
    }

    public boolean setStartAt(int eventID, LocalDateTime startAt) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        if (startAt == null) {
            throw new BadRequestException("startAt required");
        }
        return eventsRepository.setStartAt(eventID, startAt);
    }

    private void validateEventForCreateOrUpdate(Event event) {
        if (event == null) {
            throw new BadRequestException("Event required");
        }
        if (blank(event.getEventName())) {
            throw new BadRequestException("Title required");
        }
        if (blank(event.getEventDescription())) {
            throw new BadRequestException("Description required");
        }
        if (event.getStartTime() == null) {
            throw new BadRequestException("startAt required");
        }
        if (blank(event.getEventLocation())) {
            throw new BadRequestException("location required");
        }
        if (event.getCategoryID() <= 0) {
            throw new BadRequestException("categoryID required");
        }
        if (event.getMaxCapacity() != null && event.getMaxCapacity() < 0) {
            throw new BadRequestException("maxCapacity must be greater than 0");
        }
    }

    private String requireVisitor(String visitorID) {
        if (visitorID == null || visitorID.isBlank()) {
            throw new BadRequestException("Missing visitor key");
        }
        return visitorID;
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        List<String> out = new ArrayList<>(tags.size());
        for (String t : tags) {
            if (t == null) continue;
            String n = t.trim().toLowerCase();
            if (!n.isBlank()) out.add(n);
        }
        return out;
    }

    private String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase();

    }
    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
