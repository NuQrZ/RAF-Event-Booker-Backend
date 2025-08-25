package rs.raf.rafeventbooker.repositories.tags;

import rs.raf.rafeventbooker.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagsRepository {
    Optional<Tag> getTagByID(int tagID);
    Optional<Tag> getTagByName(String tagName);
    List<Tag> getAllTags();
    int createTag(String tagName);
    List<String> getTagNamesForEvent(int eventID);
}
