package rs.raf.rafeventbooker.repositories.events;

import rs.raf.rafeventbooker.model.Event;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.model.Tag;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MySQLEventsRepository extends MySQLAbstractRepository implements EventsRepository {

    private Event mapEvent(ResultSet resultSet) throws SQLException {
        return new Event(
                resultSet.getInt("event_id"),
                resultSet.getString("event_title"),
                resultSet.getString("event_description"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime() : null,
                resultSet.getTimestamp("start_at")   != null ? resultSet.getTimestamp("start_at").toLocalDateTime()   : null,
                resultSet.getString("event_location"),
                resultSet.getInt("event_views"),
                resultSet.getInt("author_id"),
                resultSet.getInt("category_id"),
                resultSet.getObject("max_capacity") != null ? resultSet.getInt("max_capacity") : null,
                resultSet.getInt("likes"),
                resultSet.getInt("dislikes")
        );
    }

    @Override
    public Optional<Event> getEventByID(int eventID) {
        String sql = "select * from events where event_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Event event = mapEvent(resultSet);
                event.setTags(getTagsForEvent(eventID));
                return Optional.of(event);
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public Page<Event> list(int page, int size) {
        int pageSize = size <= 0 ? 20 : size;
        int pageNumber = Math.max(1, page);
        int offset = (pageNumber - 1) * pageSize;

        String dataSql  = "select * from events order by created_at desc limit ? offset ?";
        String countSql = "select count(*) from events";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatementData = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet resultSetData = null;
        ResultSet resultSetCount = null;

        try {
            connection = newConnection();

            preparedStatementData = connection.prepareStatement(dataSql);
            preparedStatementData.setInt(1, pageSize);
            preparedStatementData.setInt(2, offset);
            resultSetData = preparedStatementData.executeQuery();
            while (resultSetData.next()) content.add(mapEvent(resultSetData));

            attachTags(content);

            preparedStatementCount = connection.prepareStatement(countSql);
            resultSetCount = preparedStatementCount.executeQuery();
            if (resultSetCount.next()) total = resultSetCount.getInt(1);

        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSetData);
            closeStatement(preparedStatementData);
            closeResultSet(resultSetCount);
            closeStatement(preparedStatementCount);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) pageSize);
        return new Page<>(content, pageNumber, pageSize, total, totalPages);
    }

    @Override
    public Page<Event> search(String text, int page, int size) {
        int pageSize = size <= 0 ? 20 : size;
        int pageNumber = Math.max(1, page);
        int offset = (pageNumber - 1) * pageSize;

        String likePattern = "%" + (text == null ? "" : text) + "%";
        String dataSql  = "select * from events where event_title like ? or event_description like ? order by created_at desc limit ? offset ?";
        String countSql = "select count(*) from events where event_title like ? or event_description like ?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatementData = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet resultSetData = null;
        ResultSet resultSetCount = null;

        try {
            connection = newConnection();

            preparedStatementData = connection.prepareStatement(dataSql);
            preparedStatementData.setString(1, likePattern);
            preparedStatementData.setString(2, likePattern);
            preparedStatementData.setInt(3, pageSize);
            preparedStatementData.setInt(4, offset);
            resultSetData = preparedStatementData.executeQuery();
            while (resultSetData.next()) content.add(mapEvent(resultSetData));

            attachTags(content);

            preparedStatementCount = connection.prepareStatement(countSql);
            preparedStatementCount.setString(1, likePattern);
            preparedStatementCount.setString(2, likePattern);
            resultSetCount = preparedStatementCount.executeQuery();
            if (resultSetCount.next()) total = resultSetCount.getInt(1);

        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSetData);
            closeStatement(preparedStatementData);
            closeResultSet(resultSetCount);
            closeStatement(preparedStatementCount);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) pageSize);
        return new Page<>(content, pageNumber, pageSize, total, totalPages);
    }

    @Override
    public Page<Event> listByCategory(int categoryId, int page, int size) {
        int pageSize = size <= 0 ? 20 : size;
        int pageNumber = Math.max(1, page);
        int offset = (pageNumber - 1) * pageSize;

        String dataSql  = "select * from events where category_id=? order by created_at desc limit ? offset ?";
        String countSql = "select count(*) from events where category_id=?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatementData = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet resultSetData = null;
        ResultSet resultSetCount = null;

        try {
            connection = newConnection();

            preparedStatementData = connection.prepareStatement(dataSql);
            preparedStatementData.setInt(1, categoryId);
            preparedStatementData.setInt(2, pageSize);
            preparedStatementData.setInt(3, offset);
            resultSetData = preparedStatementData.executeQuery();
            while (resultSetData.next()) content.add(mapEvent(resultSetData));

            attachTags(content);

            preparedStatementCount = connection.prepareStatement(countSql);
            preparedStatementCount.setInt(1, categoryId);
            resultSetCount = preparedStatementCount.executeQuery();
            if (resultSetCount.next()) total = resultSetCount.getInt(1);

        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSetData);
            closeStatement(preparedStatementData);
            closeResultSet(resultSetCount);
            closeStatement(preparedStatementCount);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) pageSize);
        return new Page<>(content, pageNumber, pageSize, total, totalPages);
    }

    @Override
    public Page<Event> listByTag(String tagName, int page, int size) {
        int pageSize = size <= 0 ? 20 : size;
        int pageNumber = Math.max(1, page);
        int offset = (pageNumber - 1) * pageSize;

        String dataSql =
                "select e.* " +
                        "from events e " +
                        "join event_tags et on e.event_id = et.event_id " +
                        "join tags t on t.tag_id = et.tag_id " +
                        "where t.tag_name = ? " +
                        "order by e.created_at desc " +
                        "limit ? offset ?";

        String countSql =
                "select count(distinct e.event_id) " +
                        "from events e " +
                        "join event_tags et on e.event_id = et.event_id " +
                        "join tags t on t.tag_id = et.tag_id " +
                        "where t.tag_name = ?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatementData = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet resultSetData = null;
        ResultSet resultSetCount = null;

        try {
            connection = newConnection();

            preparedStatementData = connection.prepareStatement(dataSql);
            preparedStatementData.setString(1, tagName);
            preparedStatementData.setInt(2, pageSize);
            preparedStatementData.setInt(3, offset);
            resultSetData = preparedStatementData.executeQuery();
            while (resultSetData.next()) content.add(mapEvent(resultSetData));

            attachTags(content);

            preparedStatementCount = connection.prepareStatement(countSql);
            preparedStatementCount.setString(1, tagName);
            resultSetCount = preparedStatementCount.executeQuery();
            if (resultSetCount.next()) total = resultSetCount.getInt(1);

        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSetData);
            closeStatement(preparedStatementData);
            closeResultSet(resultSetCount);
            closeStatement(preparedStatementCount);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) pageSize);
        return new Page<>(content, pageNumber, pageSize, total, totalPages);
    }

    @Override
    public List<Event> latest(int limit) {
        String sql = "select * from events order by created_at desc limit ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Event> events = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, limit);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) events.add(mapEvent(resultSet));

            attachTags(events);
            return events;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return List.of();
    }

    @Override
    public List<Event> mostViewedLast30Days(int limit) {
        String sql =
                "select e.* " +
                        "from events e " +
                        "join ( " +
                        "  select event_id, count(*) as v " +
                        "  from views_log " +
                        "  where viewed_at >= now() - interval 30 day " +
                        "  group by event_id " +
                        ") v on v.event_id = e.event_id " +
                        "order by v.v desc " +
                        "limit ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Event> events = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, limit);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) events.add(mapEvent(resultSet));

            attachTags(events);
            return events;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return List.of();
    }

    @Override
    public List<Event> mostReacted(int limit) {
        String sql = "select * from events order by (likes + dislikes) desc, created_at desc limit ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Event> events = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, limit);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) events.add(mapEvent(resultSet));

            attachTags(events);
            return events;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return List.of();
    }

    @Override
    public List<Event> similarByTags(int eventID, int limit) {
        String sql =
                "select e.* " +
                        "from events e " +
                        "join ( " +
                        "  select et2.event_id, count(*) as common_tags " +
                        "  from event_tags et1 " +
                        "  join event_tags et2 on et2.tag_id = et1.tag_id and et2.event_id <> et1.event_id " +
                        "  where et1.event_id = ? " +
                        "  group by et2.event_id " +
                        "  order by common_tags desc " +
                        "  limit ? " +
                        ") x on x.event_id = e.event_id " +
                        "order by x.common_tags desc, e.created_at desc";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Event> events = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setInt(2, limit);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) events.add(mapEvent(resultSet));

            attachTags(events);
            return events;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return List.of();
    }

    public int create(Event event, List<String> tagNames) {
        String sql =
                "insert into events(" +
                        "  event_title, event_description, created_at, start_at, event_location," +
                        "  event_views, author_id, category_id, max_capacity, likes, dislikes" +
                        ") values (?,?,now(),?,?,0,?,?,?,0,0)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            connection = newConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, event.getEventName());
            preparedStatement.setString(2, event.getEventDescription());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(event.getStartTime()));
            preparedStatement.setString(4, event.getEventLocation());
            preparedStatement.setInt(5, event.getEventAuthor());
            preparedStatement.setInt(6, event.getCategoryID());
            if (event.getMaxCapacity() == null)
                preparedStatement.setNull(7, Types.INTEGER);
            else
                preparedStatement.setInt(7, event.getMaxCapacity());

            preparedStatement.executeUpdate();

            generatedKeys = preparedStatement.getGeneratedKeys();
            if (!generatedKeys.next()) { connection.rollback(); return -1; }
            int newEventId = generatedKeys.getInt(1);
            event.setEventID(newEventId);

            upsertTagsAndBindings(connection, newEventId, tagNames);
            connection.commit();
            return newEventId;

        } catch (SQLException sqlException) {
            System.err.println("[EventsRepo.create] SQLState=" + sqlException.getSQLState() + " code=" + sqlException.getErrorCode());
            if (connection != null) try { connection.rollback(); } catch (SQLException ignore) {}
        } catch (RuntimeException runtimeException) {
            if (connection != null) try { connection.rollback(); } catch (SQLException ignore) {}
        } finally {
            if (connection != null) try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
            closeResultSet(generatedKeys);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return -1;
    }

    @Override
    public int update(Event event, List<String> tagNames) {
        String updateSql  = "update events set event_title=?, event_description=?, start_at=?, event_location=?, category_id=?, max_capacity=? where event_id=?";
        String deleteBindingsSql = "delete from event_tags where event_id=?";

        Connection connection = null;
        PreparedStatement preparedStatementUpdate = null;
        PreparedStatement preparedStatementDelete = null;

        try {
            connection = newConnection();
            connection.setAutoCommit(false);

            preparedStatementUpdate = connection.prepareStatement(updateSql);
            preparedStatementUpdate.setString(1, event.getEventName());
            preparedStatementUpdate.setString(2, event.getEventDescription());
            preparedStatementUpdate.setTimestamp(3, Timestamp.valueOf(event.getStartTime()));
            preparedStatementUpdate.setString(4, event.getEventLocation());
            preparedStatementUpdate.setInt(5, event.getCategoryID());
            if (event.getMaxCapacity() == null) preparedStatementUpdate.setNull(6, Types.INTEGER); else preparedStatementUpdate.setInt(6, event.getMaxCapacity());
            preparedStatementUpdate.setInt(7, event.getEventID());

            if (preparedStatementUpdate.executeUpdate() != 1) { connection.rollback(); return -1; }

            preparedStatementDelete = connection.prepareStatement(deleteBindingsSql);
            preparedStatementDelete.setInt(1, event.getEventID());
            preparedStatementDelete.executeUpdate();

            upsertTagsAndBindings(connection, event.getEventID(), tagNames);

            connection.commit();
            return 1;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
            if (connection != null) try { connection.rollback(); } catch (SQLException ignore) {}
        } finally {
            if (connection != null) try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
            closeStatement(preparedStatementDelete);
            closeStatement(preparedStatementUpdate);
            closeConnection(connection);
        }
        return -1;
    }

    @Override
    public boolean delete(int eventID) {
        String sql = "delete from events where event_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return false;
    }

    @Override
    public boolean incrementViewsOnce(int eventID, String visitorID) {
        String insertViewSql = "insert ignore into views_log(event_id, visitor_id, viewed_at) values(?, ?, now())";
        String incrementSql  = "update events set event_views = event_views + 1 where event_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatementInsert = null;
        PreparedStatement preparedStatementIncrement = null;

        try {
            connection = newConnection();
            connection.setAutoCommit(false);

            preparedStatementInsert = connection.prepareStatement(insertViewSql);
            preparedStatementInsert.setInt(1, eventID);
            preparedStatementInsert.setString(2, visitorID);
            int affected = preparedStatementInsert.executeUpdate();

            if (affected == 1) {
                preparedStatementIncrement = connection.prepareStatement(incrementSql);
                preparedStatementIncrement.setInt(1, eventID);
                preparedStatementIncrement.executeUpdate();
            }

            connection.commit();
            return affected == 1;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
            if (connection != null) try { connection.rollback(); } catch (SQLException ignore) {}
        } finally {
            if (connection != null) try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
            closeStatement(preparedStatementIncrement);
            closeStatement(preparedStatementInsert);
            closeConnection(connection);
        }
        return false;
    }

    @Override
    public boolean like(int eventID, String visitorID) {
        return reactToEvent(eventID, visitorID, 1);
    }

    @Override
    public boolean dislike(int eventID, String visitorID) {
        return reactToEvent(eventID, visitorID, -1);
    }

    private boolean reactToEvent(int eventID, String visitorID, int value) {
        String insertReactionSql = "insert ignore into event_reactions(event_id, visitor_id, reaction, reacted_at) values(?, ?, ?, now())";
        String incrementLikeSql = "update events set likes = likes + 1 where event_id = ?";
        String incrementDislikeSql = "update events set dislikes = dislikes + 1 where event_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatementInsert = null;
        PreparedStatement preparedStatementIncrement = null;

        try {
            connection = newConnection();
            connection.setAutoCommit(false);

            preparedStatementInsert = connection.prepareStatement(insertReactionSql);
            preparedStatementInsert.setInt(1, eventID);
            preparedStatementInsert.setString(2, visitorID);
            preparedStatementInsert.setInt(3, value);
            int affected = preparedStatementInsert.executeUpdate();

            if (affected == 0) {
                connection.rollback();
                return false;
            }

            preparedStatementIncrement = connection.prepareStatement(value > 0 ? incrementLikeSql : incrementDislikeSql);
            preparedStatementIncrement.setInt(1, eventID);
            preparedStatementIncrement.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
            if (connection != null) try { connection.rollback(); } catch (SQLException ignore) {}
        } finally {
            if (connection != null) try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
            closeStatement(preparedStatementIncrement);
            closeStatement(preparedStatementInsert);
            closeConnection(connection);
        }
        return false;
    }

    @Override
    public int capacityOf(int eventID) {
        String sql = "select coalesce(max_capacity, 0) from events where event_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt(1);
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return 0;
    }

    @Override
    public boolean setStartAt(int eventID, LocalDateTime startAt) {
        String sql = "update events set start_at = ? where event_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(startAt));
            preparedStatement.setInt(2, eventID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return false;
    }

    /* ===================== TAG helperi (List<Tag>) ===================== */

    public List<Tag> getTagsForEvent(int eventID) {
        String sql =
                "select t.tag_id, t.tag_name " +
                        "from event_tags et join tags t on t.tag_id = et.tag_id " +
                        "where et.event_id = ? order by t.tag_name";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Tag> tags = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                tags.add(new Tag(resultSet.getInt("tag_id"), resultSet.getString("tag_name")));
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }
        return tags;
    }

    private Map<Integer, List<Tag>> loadTagsMap(Connection connection, List<Integer> eventIds) throws SQLException {
        Map<Integer, List<Tag>> tagsByEventId = new HashMap<>();
        if (eventIds == null || eventIds.isEmpty()) return tagsByEventId;

        String placeholdersString = eventIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql =
                "select et.event_id, t.tag_id, t.tag_name " +
                        "from event_tags et " +
                        "join tags t on t.tag_id = et.tag_id " +
                        "where et.event_id in (" + placeholdersString + ") " +
                        "order by t.tag_name";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);
            int parameterIndex = 1;
            for (Integer id : eventIds) preparedStatement.setInt(parameterIndex++, id);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int eventIdFromRow = resultSet.getInt(1);
                Tag tag = new Tag(resultSet.getInt(2), resultSet.getString(3));
                tagsByEventId.computeIfAbsent(eventIdFromRow, k -> new ArrayList<>()).add(tag);
            }
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
        }
        return tagsByEventId;
    }

    private void attachTags(List<Event> events) {
        if (events == null || events.isEmpty()) return;

        List<Integer> eventIds = new ArrayList<>();
        for (Event event : events) eventIds.add(event.getEventID());

        Connection connection = null;
        try {
            connection = newConnection();
            Map<Integer, List<Tag>> tagsByEventId = loadTagsMap(connection, eventIds);
            for (Event event : events) {
                List<Tag> eventTags = tagsByEventId.get(event.getEventID());
                if (eventTags != null) event.setTags(eventTags);
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    private void upsertTagsAndBindings(Connection connection, int eventID, List<String> tagNames) throws SQLException {
        if (tagNames == null || tagNames.isEmpty()) return;

        String insertTagSql = "insert into tags(tag_name) values (?) on duplicate key update tag_name = tag_name";
        String selectTagIdSql = "select tag_id from tags where tag_name = ?";
        String bindSql = "insert ignore into event_tags(event_id, tag_id) values (?, ?)";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            for (String rawName : tagNames) {
                String normalizedName = normalize(rawName);
                if (normalizedName == null || normalizedName.isBlank()) continue;

                preparedStatement = connection.prepareStatement(insertTagSql);
                preparedStatement.setString(1, normalizedName);
                preparedStatement.executeUpdate();
                closeStatement(preparedStatement); preparedStatement = null;

                int tagId = 0;
                preparedStatement = connection.prepareStatement(selectTagIdSql);
                preparedStatement.setString(1, normalizedName);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) tagId = resultSet.getInt(1);
                closeResultSet(resultSet); resultSet = null;
                closeStatement(preparedStatement); preparedStatement = null;

                preparedStatement = connection.prepareStatement(bindSql);
                preparedStatement.setInt(1, eventID);
                preparedStatement.setInt(2, tagId);
                preparedStatement.executeUpdate();
                closeStatement(preparedStatement); preparedStatement = null;
            }
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
        }
    }

    private String normalize(String tagName) {
        return tagName == null ? null : tagName.trim().toLowerCase();
    }
}
