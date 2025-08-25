package rs.raf.rafeventbooker.repositories.tags;

import rs.raf.rafeventbooker.model.Tag;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLTagsRepository extends MySQLAbstractRepository implements TagsRepository {
    private Tag mapTag(ResultSet resultSet) throws SQLException {
        return new Tag(
                resultSet.getInt("tag_id"),
                resultSet.getString("tag_name")
        );
    }

    @Override
    public Optional<Tag> getTagByID(int tagID) {
        String sql = "select * from tags where tag_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, tagID);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapTag(resultSet));
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tag> getTagByName(String tagName) {
        String sql = "select * from tags where tag_name = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tagName);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapTag(resultSet));
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public List<Tag> getAllTags() {
        String sql = "select * from tags order by tag_name";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Tag> tags = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tags.add(mapTag(resultSet));
            }

            return tags;
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return List.of();
    }

    @Override
    public int createTag(String tagName) {
        String sql = "insert into tags (tag_name) values (?) on duplicate key update tag_name = tag_name";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, normalize(tagName));

            preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return -1;
    }

    @Override
    public List<String> getTagNamesForEvent(int eventID) {
        String sql = "select t.tag_name from tags t join event_tags et on t.tag_id = et.tag_id where et.event_id = ? order by t.tag_name";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<String> tagNames = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tagNames.add(resultSet.getString(1));
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return List.of();
    }

    private String normalize(String tagName) {
        return tagName == null ? null : tagName.trim().toLowerCase();
    }
}
