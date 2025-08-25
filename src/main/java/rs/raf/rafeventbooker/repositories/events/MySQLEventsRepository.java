package rs.raf.rafeventbooker.repositories.events;

import rs.raf.rafeventbooker.model.Event;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLEventsRepository extends MySQLAbstractRepository implements EventsRepository {
    private Event mapEvent(ResultSet resultSet) throws SQLException {
        return new Event(
                resultSet.getInt("event_id"),
                resultSet.getString("event_title"),
                resultSet.getString("event_description"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime() : null,
                resultSet.getTimestamp("start_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime() : null,
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
                return Optional.of(mapEvent(resultSet));
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
    public Page<Event> list(int page, int size) {
        int checkSize = size <= 0 ? 20 : size;
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * checkSize;

        String data = "SELECT * FROM events ORDER BY created_at DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) FROM events";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();

            preparedStatement = connection.prepareStatement(data);
            preparedStatement.setInt(1, checkSize);
            preparedStatement.setInt(2, offset);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                content.add(mapEvent(resultSet));
            }

            preparedStatement = connection.prepareStatement(count);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) total = resultSet.getInt(1);

        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) checkSize);
        return new Page<>(content, safePage, checkSize, total, totalPages);
    }

    @Override
    public Page<Event> search(String text, int page, int size) {
        int checkSize = size <= 0 ? 20 : size;
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * checkSize;

        String like = "%" + (text == null ? "" : text) + "%";
        String count = "SELECT COUNT(*) FROM events WHERE event_name LIKE ? OR event_description LIKE ?";
        String pageSql = "SELECT * FROM events WHERE event_name LIKE ? OR event_description LIKE ? " +
                "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();

            preparedStatement = connection.prepareStatement(pageSql);
            preparedStatement.setString(1, like);
            preparedStatement.setString(2, like);
            preparedStatement.setInt(3, checkSize);
            preparedStatement.setInt(4, offset);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                content.add(mapEvent(resultSet));
            }

            preparedStatement = connection.prepareStatement(count);
            preparedStatement.setString(1, like);
            preparedStatement.setString(2, like);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) total = resultSet.getInt(1);

        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) checkSize);
        return new Page<>(content, safePage, checkSize, total, totalPages);
    }

    @Override
    public Page<Event> listByCategory(int categoryId, int page, int size) {
        int checkSize = size <= 0 ? 20 : size;
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * checkSize;

        String data = "SELECT * FROM events WHERE category_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) FROM events WHERE category_id = ?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();

            preparedStatement = connection.prepareStatement(data);
            preparedStatement.setInt(1, categoryId);
            preparedStatement.setInt(2, checkSize);
            preparedStatement.setInt(3, offset);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                content.add(mapEvent(resultSet));
            }

            preparedStatement = connection.prepareStatement(count);
            preparedStatement.setInt(1, categoryId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) total = resultSet.getInt(1);

        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) checkSize);
        return new Page<>(content, safePage, checkSize, total, totalPages);
    }

    @Override
    public Page<Event> listByTag(String tagName, int page, int size) {
        int checkSize = size <= 0 ? 20 : size;
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * checkSize;

        String data = "SELECT e.* FROM events e " +
                "JOIN event_tags et ON e.event_id = et.event_id " +
                "JOIN tags t ON t.tag_id = et.tag_id " +
                "WHERE t.tag_name = ? " +
                "ORDER BY e.created_at DESC " +
                "LIMIT ? OFFSET ?";

        String count = "SELECT COUNT(*) FROM events e " +
                "JOIN event_tags et ON e.event_id = et.event_id " +
                "JOIN tags t ON t.tag_id = et.tag_id " +
                "WHERE t.tag_name = ?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();

            preparedStatement = connection.prepareStatement(data);
            preparedStatement.setString(1, tagName);
            preparedStatement.setInt(2, checkSize);
            preparedStatement.setInt(3, offset);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                content.add(mapEvent(resultSet));
            }

            preparedStatement = connection.prepareStatement(count);
            preparedStatement.setString(1, tagName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) total = resultSet.getInt(1);

        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            closeResultSet(resultSet);
            closeStatement(preparedStatement);
            closeConnection(connection);
        }

        int totalPages = (int) Math.ceil(total / (double) checkSize);
        return new Page<>(content, safePage, checkSize, total, totalPages);
    }


    @Override
    public List<Event> latest(int limit) {
        String sql = "select * from events order by created_at DESC limit ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Event> eventList = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, limit);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventList.add(mapEvent(resultSet));
            }

            return eventList;
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
    public List<Event> mostViewedLast30Days(int limit) {
        String sql = "select e.* from events e " +
                "join (" +
                "select event_id, sum(1) as v " +
                "from views_log " +
                "where viewed_at >= now() - interval 30 day " +
                "group by event_id" +
                ") v on v.event_id = e.event_id " +
                "order by v.v desc " +
                "limit ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Event> eventList = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, limit);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventList.add(mapEvent(resultSet));
            }

            return eventList;
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
    public List<Event> mostReacted(int limit) {
        String sql = "select e.* from events e order by (e.likes + e.dislikes) desc, e.created_at desc limit ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Event> eventList = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, limit);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventList.add(mapEvent(resultSet));
            }

            return eventList;
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
    public List<Event> similarByTags(int eventID, int limit) {
        String sql = "select distinct * from event_tags et1 join event_tags et2 on et1.tag_id = et2.tag_id and et2.event_id <> et1.event_id join events e on e.event_id = et2.event_id where et1.event_id = ? order by e.created_at desc limit ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Event> eventList = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setInt(2, limit);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventList.add(mapEvent(resultSet));
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

    @Override
    public int create(Event event, List<String> tags) {
        String sql = "insert into events(event_name, event_description, created_at, start_at, event_location, event_views, author_id, category_id, max_capacity, likes, dislikes) values (?,?,?,?,?,0,?,?,?,0,0)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, event.getEventName());
            preparedStatement.setString(2, event.getEventDescription());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(event.getCreatedAt()));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(event.getStartTime()));
            preparedStatement.setString(5, event.getEventLocation());
            preparedStatement.setInt(6, event.getEventAuthor());
            preparedStatement.setInt(7, event.getCategoryID());
            if(event.getMaxCapacity() == null) {
                preparedStatement.setNull(8, Types.INTEGER);
            }
            else {
                preparedStatement.setInt(8, event.getMaxCapacity());
            }
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                event.setEventID(resultSet.getInt(1));
                return resultSet.getInt(1);
            }

            upsertTagsAndBindings(event.getEventID(), tags);
            connection.commit();
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
    public int update(Event e, List<String> tags) {
        String sql = "update events set event_name = ?, event_description = ?, start_at = ?, event_location = ?, category_id = ?, max_capacity = ? where event_id = ?";
        String delSql = "delete from event_tags where event_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, e.getEventName());
            preparedStatement.setString(2, e.getEventDescription());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(e.getStartTime()));
            preparedStatement.setString(4, e.getEventLocation());
            preparedStatement.setInt(5, e.getCategoryID());
            if(e.getMaxCapacity() == null) {
                preparedStatement.setNull(6, Types.INTEGER);
            } else  {
                preparedStatement.setInt(6, e.getMaxCapacity());
            }
            preparedStatement.setInt(7, e.getEventID());
            if (preparedStatement.executeUpdate() != 1) {
                connection.rollback();
                return -1;
            }

            preparedStatement = connection.prepareStatement(delSql);
            preparedStatement.setInt(1, e.getEventID());

            upsertTagsAndBindings(e.getEventID(), tags);
            connection.commit();
            return 0;
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
    public boolean delete(int eventID) {
        String sql = "delete from events where event_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return false;
    }

    @Override
    public boolean incrementViewsOnce(int eventID, String visitorID) {
        String check = "select 1 from views_log where event_id = ? and visitor_id = ?";
        String insertIntoViews = "insert into views_log (event_id, visitor_id, viewed_at) values (?, ?, now())";
        String incrementViews = "update events set event_views = event_views + 1 where event_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(check);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setString(2, visitorID);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                connection.rollback();
                return false;
            }

            preparedStatement = connection.prepareStatement(insertIntoViews);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setString(2, visitorID);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(incrementViews);
            preparedStatement.setInt(1, eventID);
            preparedStatement.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
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
        String check = "select 1 from event_reactions where event_id = ? and visitor_id = ?";
        String insert = "insert into event_reactions(event_id, visitor_id, reaction, reacted_at) VALUES (?, ?, ?, now())";
        String increase = "";
        if (value > 0) {
            increase = "update events set likes = likes + 1 where event_id = ?";
        } else {
            increase = "update events set dislikes = dislikes + 1 where event_id = ?";
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(check);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setString(2, visitorID);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                connection.rollback();
                return false;
            }

            preparedStatement = connection.prepareStatement(insert);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setString(2, visitorID);
            preparedStatement.setInt(3, value);

            preparedStatement = connection.prepareStatement(increase);
            return true;
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return false;
    }

    @Override
    public int countRsvp(int eventID) {
        String sql = "SELECT count(*) from rsvps where event_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);

            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return 0;
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
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return 0;
    }

    @Override
    public boolean setStartAt(int eventID, LocalDateTime startAt) {
        String sql = "update events set start_at = ? where event_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(startAt));
            preparedStatement.setInt(2, eventID);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return false;
    }

    private void upsertTagsAndBindings(int eventID, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        String insertTag = "insert into tags(tag_name) values (?) on duplicate key update tag_name = tag_name";
        String selectTag = "select tag_name from tags where tag_name = ?";
        String bindTag = "insert ignore into event_tags(event_id, tag_id) values (?, ?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            for (String rawTagName : tags) {
                String tagName = normalize(rawTagName);

                preparedStatement = connection.prepareStatement(insertTag);
                preparedStatement.setString(1, tagName);
                preparedStatement.executeUpdate();

                int tagID = 0;
                preparedStatement = connection.prepareStatement(selectTag);
                preparedStatement.setString(1, tagName);

                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                tagID = resultSet.getInt(1);

                preparedStatement = connection.prepareStatement(bindTag);
                preparedStatement.setInt(1, eventID);
                preparedStatement.setInt(2, tagID);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    private String normalize(String tagName) {
        return tagName == null ? null : tagName.trim().toLowerCase();
    }

}
