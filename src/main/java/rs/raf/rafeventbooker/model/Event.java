package rs.raf.rafeventbooker.model;

import java.time.LocalDateTime;
import java.util.List;

public class Event {
    private Integer eventID;
    private String eventName;
    private String eventDescription;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private String eventLocation;
    private Integer eventViews;
    private Integer eventAuthor;
    private Integer categoryID;
    private Integer maxCapacity;
    private Integer likes;
    private Integer dislikes;
    private List<Tag> tags;

    public Event() {

    }

    public Event(Integer eventID, String eventName, String eventDescription, LocalDateTime createdAt, LocalDateTime startTime, String eventLocation, Integer eventViews, Integer eventAuthor, Integer categoryID, Integer maxCapacity, Integer likes, Integer dislikes) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.createdAt = createdAt;
        this.startTime = startTime;
        this.eventLocation = eventLocation;
        this.eventViews = eventViews;
        this.eventAuthor = eventAuthor;
        this.categoryID = categoryID;
        this.maxCapacity = maxCapacity;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public Integer getEventViews() {
        return eventViews;
    }

    public void setEventViews(Integer eventViews) {
        this.eventViews = eventViews;
    }

    public Integer getEventAuthor() {
        return eventAuthor;
    }

    public void setEventAuthor(Integer eventAuthor) {
        this.eventAuthor = eventAuthor;
    }

    public Integer getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Integer categoryID) {
        this.categoryID = categoryID;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
