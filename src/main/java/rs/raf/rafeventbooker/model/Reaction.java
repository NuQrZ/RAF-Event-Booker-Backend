package rs.raf.rafeventbooker.model;

import rs.raf.rafeventbooker.model.enums.ReactionType;

import java.time.LocalDateTime;

public class Reaction {
    private String reactionID;
    private String visitorID;
    private String eventID;
    private ReactionType reactionType;
    private LocalDateTime reactedAt;

    public Reaction(String reactionID, String visitorID, String eventID, ReactionType reactionType, LocalDateTime reactedAt) {
        this.reactionID = reactionID;
        this.visitorID = visitorID;
        this.eventID = eventID;
        this.reactionType = reactionType;
        this.reactedAt = reactedAt;
    }

    public String getReactionID() {
        return reactionID;
    }

    public void setReactionID(String reactionID) {
        this.reactionID = reactionID;
    }

    public String getVisitorID() {
        return visitorID;
    }

    public void setVisitorID(String visitorID) {
        this.visitorID = visitorID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public LocalDateTime getReactedAt() {
        return reactedAt;
    }

    public void setReactedAt(LocalDateTime reactedAt) {
        this.reactedAt = reactedAt;
    }
}
