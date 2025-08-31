package rs.raf.rafeventbooker.services;

import rs.raf.rafeventbooker.model.Comment;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.repositories.comments.CommentRepository;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

public class CommentService {
    @Inject
    private CommentRepository commentRepository;

    public Page<Comment> getCommentsForEvent(int eventID, int page, int size) {
        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : Math.min(size, 100);
        return commentRepository.getCommentsForEvent(eventID, p, s);
    }

    public Optional<Comment> getCommentByID(int commentID) {
        if (commentID <= 0) {
            throw new BadRequestException("Invalid commentID");
        }
        return commentRepository.getCommentById(commentID);
    }

    public int createComment(Comment comment) {
        if (comment == null) {
            throw new BadRequestException("Comment is required");
        }
        int eventID = comment.getEventID();
        String authorName = comment.getCommentAuthor();
        String content = comment.getCommentContent();

        if (eventID <= 0) {
            throw new BadRequestException("Invalid eventID");
        }
        if (authorName == null || authorName.isBlank()) {
            throw new BadRequestException("Author name required");
        }
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Content required");
        }

        String safeAuthor = authorName.trim();
        String safeContent = content.trim();
        if (safeAuthor.length() > 100) safeAuthor = safeAuthor.substring(0, 100);
        if (safeContent.length() > 2000) safeContent = safeContent.substring(0, 2000);

        Comment newComment = new Comment();
        newComment.setCommentID(comment.getCommentID());
        newComment.setEventID(eventID);
        newComment.setCommentAuthor(safeAuthor);
        newComment.setCommentContent(safeContent);
        newComment.setCreatedAt(LocalDateTime.now());
        newComment.setLikeCount(comment.getLikeCount());
        newComment.setDislikeCount(comment.getDislikeCount());

        int id = commentRepository.createComment(newComment);
        if (id <= 0) {
            throw new BadRequestException("Failed to create comment");
        }
        return id;
    }

    public void deleteComment(int commentID) {
        if (commentID <= 0) {
            throw new BadRequestException("Invalid commentID");
        }
        boolean ok = commentRepository.deleteComment(commentID);
        if (!ok) {
            throw new NotFoundException("Comment not found");
        }
    }

    public void updateCommentContent(int commentID, String newContent) {
        if (commentID <= 0) {
            throw new BadRequestException("Invalid commentID");
        }
        if (newContent == null || newContent.isBlank()) {
            throw new BadRequestException("Content required");
        }

        String safe = newContent.trim();
        if (safe.length() > 2000) safe = safe.substring(0, 2000);

        boolean ok = commentRepository.updateCommentContent(commentID, safe);
        if (!ok) {
            throw new NotFoundException("Comment not found");
        }
    }

    public boolean likeComment(int commentID, String visitorId) {
        if (commentID <= 0) {
            throw new BadRequestException("Invalid commentID");
        }
        String vk = requireVisitor(visitorId);
        return commentRepository.like(commentID, vk);
    }

    public boolean dislikeComment(int commentID, String visitorId) {
        if (commentID <= 0) {
            throw new BadRequestException("Invalid commentID");
        }
        String vk = requireVisitor(visitorId);
        return commentRepository.dislike(commentID, vk);
    }

    private String requireVisitor(String visitorID) {
        if (visitorID == null || visitorID.isBlank()) {
            throw new BadRequestException("Missing visitor key");
        }
        return visitorID;
    }
}
