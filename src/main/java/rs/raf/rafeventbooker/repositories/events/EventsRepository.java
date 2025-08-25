package rs.raf.rafeventbooker.repositories.events;

import rs.raf.rafeventbooker.model.Event;
import rs.raf.rafeventbooker.model.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventsRepository {
    Optional<Event> getEventByID(int eventID);
    Page<Event> list(int page, int size);
    Page<Event> search(String text, int page, int size);
    Page<Event> listByCategory(int categoryId, int page, int size);
    Page<Event> listByTag(String tagName, int page, int size);
    List<Event> latest(int limit);
    List<Event> mostViewedLast30Days(int limit);
    List<Event> mostReacted(int limit);
    List<Event> similarByTags(int eventID, int limit);
    int create(Event e, List<String> tags);
    int update(Event e, List<String> tags);
    boolean delete(int eventID);
    boolean incrementViewsOnce(int eventID, String visitorID);
    boolean like(int eventID, String visitorID);
    boolean dislike(int eventID, String visitorID);
    int countRsvp(int eventID);
    int capacityOf(int eventID);
    boolean setStartAt(int eventID, LocalDateTime startAt);
}
