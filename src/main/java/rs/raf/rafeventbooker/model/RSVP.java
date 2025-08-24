package rs.raf.rafeventbooker.model;

import java.time.LocalDateTime;

public class RSVP {
    private Integer id;
    private String userEmail;
    private Integer eventID;
    private LocalDateTime createdAt;

    public RSVP() {

    }

    public RSVP(Integer id, String userEmail, Integer eventID, LocalDateTime createdAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.eventID = eventID;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}