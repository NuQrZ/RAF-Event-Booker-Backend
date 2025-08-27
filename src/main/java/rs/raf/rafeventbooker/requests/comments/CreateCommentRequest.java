package rs.raf.rafeventbooker.requests.comments;

import javax.validation.constraints.NotBlank;

public class CreateCommentRequest {
    @NotBlank public String authorName;
    @NotBlank public String content;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
