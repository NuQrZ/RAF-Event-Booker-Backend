package rs.raf.rafeventbooker.model;

import java.time.LocalDateTime;

public class RSVP {
    private Integer eventID;
    private String userEmail;
    private LocalDateTime createdAt;

    public RSVP() {

    }

    public RSVP(Integer eventID, String userEmail, LocalDateTime createdAt) {
        this.eventID = eventID;
        this.userEmail = userEmail;
        this.createdAt = createdAt;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}