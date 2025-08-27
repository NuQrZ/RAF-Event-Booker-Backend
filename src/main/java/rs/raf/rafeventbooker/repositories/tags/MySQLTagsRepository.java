package rs.raf.rafeventbooker.repositories.tags;

import rs.raf.rafeventbooker.model.Tag;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLTagsRepository extends MySQLAbstractRepository implements TagsRepository {

    private Tag mapTag(ResultSet rs) throws SQLException {
        return new Tag(
                rs.getInt("tag_id"),
                rs.getString("tag_name")
        );
    }

    @Override
    public Optional<Tag> getTagByID(int tagID) {
        String sql = "select tag_id, tag_name from tags where tag_id = ?";
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, tagID);
            rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapTag(rs));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(c);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tag> getTagByName(String tagName) {
        String sql = "select tag_id, tag_name from tags where tag_name = ? limit 1";
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setString(1, normalize(tagName));
            rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapTag(rs));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(c);
        }
        return Optional.empty();
    }

    @Override
    public List<Tag> getAllTags() {
        String sql = "select tag_id, tag_name from tags order by tag_name";
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        List<Tag> tags = new ArrayList<>();
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) tags.add(mapTag(rs));
            return tags;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(c);
        }
        return List.of();
    }

    @Override
    public int createTag(String tagName) {
        String sql = "insert into tags(tag_name) values (?) " +
                "on duplicate key update tag_id = last_insert_id(tag_id)";
        Connection c = null; PreparedStatement ps = null; ResultSet keys = null;
        try {
            String norm = normalize(tagName);
            if (norm == null || norm.isBlank()) return -1;

            c = newConnection();
            ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, norm);
            ps.executeUpdate();

            keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(keys);
            this.closeStatement(ps);
            this.closeConnection(c);
        }
        return -1;
    }

    @Override
    public List<String> getTagNamesForEvent(int eventID) {
        String sql = "select t.tag_name " +
                "from tags t " +
                "join event_tags et on t.tag_id = et.tag_id " +
                "where et.event_id = ? " +
                "order by t.tag_name";
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        List<String> tagNames = new ArrayList<>();
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, eventID);
            rs = ps.executeQuery();
            while (rs.next()) tagNames.add(rs.getString(1));
            return tagNames;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(c);
        }
        return List.of();
    }

    private String normalize(String tagName) {
        return tagName == null ? null : tagName.trim().toLowerCase();
    }
}
