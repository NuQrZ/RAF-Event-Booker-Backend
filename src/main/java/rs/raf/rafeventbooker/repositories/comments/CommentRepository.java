package rs.raf.rafeventbooker.repositories.comments;

import rs.raf.rafeventbooker.model.Comment;
import rs.raf.rafeventbooker.model.Page;

import java.util.Optional;

public interface CommentRepository {
    Optional<Comment> getCommentById(int commentID);
    Page<Comment> getCommentsForEvent(int eventID, int page, int size);
    int createComment(Comment comment);
    boolean updateCommentContent(int commentID, String content);
    boolean deleteComment(int commentID);
    boolean like(int commentID, String visitorID);
    boolean dislike(int commentID, String visitorID);
}
