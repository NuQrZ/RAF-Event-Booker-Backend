package rs.raf.rafeventbooker.model;

public class Tag {
    private Integer tagID;
    private String tagName;

    public Tag(Integer tagID, String tagName) {
        this.tagID = tagID;
        this.tagName = tagName;
    }

    public Integer getTagID() {
        return tagID;
    }

    public void setTagID(Integer tagID) {
        this.tagID = tagID;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
