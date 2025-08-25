package rs.raf.rafeventbooker.model;

import java.time.LocalDateTime;

public class Comment {
    private Integer commentID;
    private String commentAuthor;
    private String commentContent;
    private LocalDateTime createdAt;
    private Integer eventID;
    private Integer likeCount;
    private Integer dislikeCount;

    public Comment() {

    }

    public Comment(Integer commentID, Integer eventID, String commentAuthor, String commentContent, LocalDateTime createdAt, Integer likeCount, Integer dislikeCount) {
        this.commentID = commentID;
        this.eventID = eventID;
        this.commentAuthor = commentAuthor;
        this.commentContent = commentContent;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }

    public Integer getCommentID() {
        return commentID;
    }

    public void setCommentID(Integer commentID) {
        this.commentID = commentID;
    }

    public String getCommentAuthor() {
        return commentAuthor;
    }

    public void setCommentAuthor(String commentAuthor) {
        this.commentAuthor = commentAuthor;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(Integer dislikeCount) {
        this.dislikeCount = dislikeCount;
    }
}
