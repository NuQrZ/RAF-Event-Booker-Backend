package rs.raf.rafeventbooker.repositories.rsvp;

import rs.raf.rafeventbooker.model.RSVP;

import java.util.Optional;

public interface RsvpRepository {
    int createRsvp(RSVP rsvp);
    boolean deleteRsvp(int eventID, String userEmail);
    int countRSVPsByEvent(int eventID);
    boolean rsvpExists(int eventID, String userEmail);
    Optional<RSVP> getRSVP(int eventID, String userEmail);
}
