package rs.raf.rafeventbooker.requests.comments;

import javax.validation.constraints.NotBlank;

public record CreateCommentRequest (
    @NotBlank String authorName,
    @NotBlank String content
) {}