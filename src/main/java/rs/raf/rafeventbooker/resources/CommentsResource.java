package rs.raf.rafeventbooker.resources;

import rs.raf.rafeventbooker.model.Comment;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.requests.comments.CreateCommentRequest;
import rs.raf.rafeventbooker.requests.comments.UpdateCommentRequest;
import rs.raf.rafeventbooker.services.CommentService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;

@Path("/public/events/{eventID}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentsResource {

    @Inject private CommentService service;

    @GET
    public Page<Comment> listComments(@PathParam("eventID") int eventID,
                              @QueryParam("page") @DefaultValue("1") int page,
                              @QueryParam("size") @DefaultValue("10") int size) {
        return service.getCommentsForEvent(eventID, page, size);
    }

    @GET @Path("/{commentID}")
    public Response getComment(@PathParam("commentID") int commentID) {
        Comment c = service.getCommentByID(commentID)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        return Response.ok(c).build();
    }

    @POST
    public Response createComment(@PathParam("eventID") int eventID,
                           @Valid CreateCommentRequest body,
                           @Context UriInfo uri) {
        Comment c = new Comment();
        c.setEventID(eventID);
        c.setCommentAuthor(body.authorName());
        c.setCommentContent(body.content());

        int id = service.createComment(c);

        URI location = uri.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(location).build();
    }

    @PUT @Path("/{commentID}")
    public Response updateComment(@PathParam("commentID") int commentID,
                           @Valid UpdateCommentRequest body) {
        service.updateCommentContent(commentID, body.content());
        return Response.noContent().build();
    }

    @DELETE @Path("/{commentID}")
    public Response deleteComment(@PathParam("commentID") int commentID) {
        service.deleteComment(commentID);
        return Response.noContent().build();
    }

    @POST @Path("/{commentID}/like")
    public Response like(@PathParam("commentID") int commentID,
                         @HeaderParam("VisitorId") String visitorID) {
        boolean ok = service.likeComment(commentID, visitorID);
        return ok ? Response.noContent().build() : Response.status(Response.Status.CONFLICT).build();
    }

    @POST @Path("/{commentID}/dislike")
    public Response dislike(@PathParam("commentID") int commentID,
                            @HeaderParam("VisitorId") String visitorID) {
        boolean ok = service.dislikeComment(commentID, visitorID);
        return ok ? Response.noContent().build() : Response.status(Response.Status.CONFLICT).build();
    }
}
