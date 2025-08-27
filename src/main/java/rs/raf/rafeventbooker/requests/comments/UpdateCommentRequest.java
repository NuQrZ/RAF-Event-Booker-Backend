package rs.raf.rafeventbooker.requests.comments;

import javax.validation.constraints.NotBlank;

public class UpdateCommentRequest {
    @NotBlank public String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
