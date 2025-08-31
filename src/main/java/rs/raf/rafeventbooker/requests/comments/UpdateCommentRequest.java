package rs.raf.rafeventbooker.requests.comments;

import javax.validation.constraints.NotBlank;

public record UpdateCommentRequest (
    @NotBlank String content
) {}
