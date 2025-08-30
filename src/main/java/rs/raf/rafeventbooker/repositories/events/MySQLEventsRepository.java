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

    private Event mapEvent(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("event_id"),
                rs.getString("event_title"),
                rs.getString("event_description"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("start_at")   != null ? rs.getTimestamp("start_at").toLocalDateTime()   : null, // ✔️
                rs.getString("event_location"),
                rs.getInt("event_views"),
                rs.getInt("author_id"),
                rs.getInt("category_id"),
                rs.getObject("max_capacity") != null ? rs.getInt("max_capacity") : null,
                rs.getInt("likes"),
                rs.getInt("dislikes")
        );
    }

    @Override
    public Optional<Event> getEventByID(int eventID) {
        String sql = "select * from events where event_id = ?";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, eventID);
            rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapEvent(rs));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(c);
        }
        return Optional.empty();
    }

    @Override
    public Page<Event> list(int page, int size) {
        int s = size <= 0 ? 20 : size;
        int p = Math.max(1, page);
        int offset = (p - 1) * s;

        String dataSql  = "select * from events order by created_at desc limit ? offset ?";
        String countSql = "select count(*) from events";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection c = null;
        PreparedStatement psData = null, psCount = null;
        ResultSet rsData = null, rsCount = null;

        try {
            c = newConnection();

            psData = c.prepareStatement(dataSql);
            psData.setInt(1, s);
            psData.setInt(2, offset);
            rsData = psData.executeQuery();
            while (rsData.next()) content.add(mapEvent(rsData));

            psCount = c.prepareStatement(countSql);
            rsCount = psCount.executeQuery();
            if (rsCount.next()) total = rsCount.getInt(1);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rsData);
            closeStatement(psData);
            closeResultSet(rsCount);
            closeStatement(psCount);
            closeConnection(c);
        }

        int totalPages = (int) Math.ceil(total / (double) s);
        return new Page<>(content, p, s, total, totalPages);
    }

    @Override
    public Page<Event> search(String text, int page, int size) {
        int s = size <= 0 ? 20 : size;
        int p = Math.max(1, page);
        int offset = (p - 1) * s;

        String like = "%" + (text == null ? "" : text) + "%";
        String dataSql  = "select * from events where event_title like ? or event_description like ? " +
                "order by created_at desc limit ? offset ?";
        String countSql = "select count(*) from events where event_title like ? or event_description like ?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection c = null;
        PreparedStatement psData = null, psCount = null;
        ResultSet rsData = null, rsCount = null;

        try {
            c = newConnection();

            psData = c.prepareStatement(dataSql);
            psData.setString(1, like);
            psData.setString(2, like);
            psData.setInt(3, s);
            psData.setInt(4, offset);
            rsData = psData.executeQuery();
            while (rsData.next()) content.add(mapEvent(rsData));

            psCount = c.prepareStatement(countSql);
            psCount.setString(1, like);
            psCount.setString(2, like);
            rsCount = psCount.executeQuery();
            if (rsCount.next()) total = rsCount.getInt(1);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rsData);
            closeStatement(psData);
            closeResultSet(rsCount);
            closeStatement(psCount);
            closeConnection(c);
        }

        int totalPages = (int) Math.ceil(total / (double) s);
        return new Page<>(content, p, s, total, totalPages);
    }

    @Override
    public Page<Event> listByCategory(int categoryId, int page, int size) {
        int s = size <= 0 ? 20 : size;
        int p = Math.max(1, page);
        int offset = (p - 1) * s;

        String dataSql  = "select * from events where category_id=? order by created_at desc limit ? offset ?";
        String countSql = "select count(*) from events where category_id=?";

        List<Event> content = new ArrayList<>();
        int total = 0;

        Connection c = null;
        PreparedStatement psData = null, psCount = null;
        ResultSet rsData = null, rsCount = null;

        try {
            c = newConnection();

            psData = c.prepareStatement(dataSql);
            psData.setInt(1, categoryId);
            psData.setInt(2, s);
            psData.setInt(3, offset);
            rsData = psData.executeQuery();
            while (rsData.next()) content.add(mapEvent(rsData));

            psCount = c.prepareStatement(countSql);
            psCount.setInt(1, categoryId);
            rsCount = psCount.executeQuery();
            if (rsCount.next()) total = rsCount.getInt(1);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rsData);
            closeStatement(psData);
            closeResultSet(rsCount);
            closeStatement(psCount);
            closeConnection(c);
        }

        int totalPages = (int) Math.ceil(total / (double) s);
        return new Page<>(content, p, s, total, totalPages);
    }

    @Override
    public Page<Event> listByTag(String tagName, int page, int size) {
        int s = size <= 0 ? 20 : size;
        int p = Math.max(1, page);
        int offset = (p - 1) * s;

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

        Connection c = null;
        PreparedStatement psData = null, psCount = null;
        ResultSet rsData = null, rsCount = null;

        try {
            c = newConnection();

            psData = c.prepareStatement(dataSql);
            psData.setString(1, tagName);
            psData.setInt(2, s);
            psData.setInt(3, offset);
            rsData = psData.executeQuery();
            while (rsData.next()) content.add(mapEvent(rsData));

            psCount = c.prepareStatement(countSql);
            psCount.setString(1, tagName);
            rsCount = psCount.executeQuery();
            if (rsCount.next()) total = rsCount.getInt(1);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rsData);
            closeStatement(psData);
            closeResultSet(rsCount);
            closeStatement(psCount);
            closeConnection(c);
        }

        int totalPages = (int) Math.ceil(total / (double) s);
        return new Page<>(content, p, s, total, totalPages);
    }

    @Override
    public List<Event> latest(int limit) {
        String sql = "select * from events order by created_at desc limit ?";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Event> list = new ArrayList<>();
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapEvent(rs));
            return list;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(c);
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
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Event> list = new ArrayList<>();
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapEvent(rs));
            return list;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(c);
        }
        return List.of();
    }

    @Override
    public List<Event> mostReacted(int limit) {
        String sql = "select * from events order by (likes + dislikes) desc, created_at desc limit ?";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Event> list = new ArrayList<>();
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapEvent(rs));
            return list;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(c);
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

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Event> list = new ArrayList<>();
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, eventID);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapEvent(rs));
            return list;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(c);
        }
        return List.of();
    }


    public int create(Event e, List<String> tags) {
        String sql =
                "insert into events(" +
                        "  event_title, event_description, created_at, start_at, event_location," +
                        "  event_views, author_id, category_id, max_capacity, likes, dislikes" +
                        ") values (?,?,now(),?,?,0,?,?,?,0,0)";

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet keys = null;

        try {
            c = newConnection();
            c.setAutoCommit(false);

            ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, e.getEventName());                           // 1: title
            ps.setString(2, e.getEventDescription());                    // 2: description
            ps.setTimestamp(3, Timestamp.valueOf(e.getStartTime()));     // 3: start_at
            ps.setString(4, e.getEventLocation());                       // 4: location
            ps.setInt(5, e.getEventAuthor());                            // 5: author_id
            ps.setInt(6, e.getCategoryID());                             // 6: category_id
            if (e.getMaxCapacity() == null)                              // 7: max_capacity
                ps.setNull(7, Types.INTEGER);
            else
                ps.setInt(7, e.getMaxCapacity());

            ps.executeUpdate();

            keys = ps.getGeneratedKeys();
            if (!keys.next()) { c.rollback(); return -1; }
            int id = keys.getInt(1);
            e.setEventID(id);

            upsertTagsAndBindings(c, id, tags);
            c.commit();
            return id;

        } catch (SQLException ex) {
            // Log sa više detalja, lakše ćeš naći uzrok
            System.err.println("[EventsRepo.create] SQLState=" + ex.getSQLState() + " code=" + ex.getErrorCode());
            ex.printStackTrace();
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
        } catch (RuntimeException rex) {
            // hvata NPE i slične
            rex.printStackTrace();
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
        } finally {
            if (c != null) try { c.setAutoCommit(true); } catch (SQLException ignore) {}
            closeResultSet(keys);
            closeStatement(ps);
            closeConnection(c);
        }
        return -1;
    }


    @Override
    public int update(Event e, List<String> tags) {
        String upSql  = "update events set event_title=?, event_description=?, start_at=?, event_location=?, category_id=?, max_capacity=? where event_id=?";
        String delSql = "delete from event_tags where event_id=?";

        Connection c = null;
        PreparedStatement psUp = null, psDel = null;

        try {
            c = newConnection();
            c.setAutoCommit(false);

            psUp = c.prepareStatement(upSql);
            psUp.setString(1, e.getEventName());
            psUp.setString(2, e.getEventDescription());
            psUp.setTimestamp(3, Timestamp.valueOf(e.getStartTime()));
            psUp.setString(4, e.getEventLocation());
            psUp.setInt(5, e.getCategoryID());
            if (e.getMaxCapacity() == null) psUp.setNull(6, Types.INTEGER); else psUp.setInt(6, e.getMaxCapacity());
            psUp.setInt(7, e.getEventID());

            if (psUp.executeUpdate() != 1) { c.rollback(); return -1; }

            psDel = c.prepareStatement(delSql);
            psDel.setInt(1, e.getEventID());
            psDel.executeUpdate();

            upsertTagsAndBindings(c, e.getEventID(), tags);

            c.commit();
            return 1;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
        } finally {
            if (c != null) try { c.setAutoCommit(true); } catch (SQLException ignore) {}
            closeStatement(psDel);
            closeStatement(psUp);
            closeConnection(c);
        }
        return -1;
    }

    @Override
    public boolean delete(int eventID) {
        String sql = "delete from events where event_id = ?";
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, eventID);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeStatement(ps);
            closeConnection(c);
        }
        return false;
    }

    @Override
    public boolean incrementViewsOnce(int eventID, String visitorID) {
        String insView = "insert ignore into views_log(event_id, visitor_id, viewed_at) values(?, ?, now())";
        String inc     = "update events set event_views = event_views + 1 where event_id = ?";

        Connection c = null;
        PreparedStatement psIns = null, psInc = null;

        try {
            c = newConnection();
            c.setAutoCommit(false);

            psIns = c.prepareStatement(insView);
            psIns.setInt(1, eventID);
            psIns.setString(2, visitorID);
            int affected = psIns.executeUpdate();

            if (affected == 1) {
                psInc = c.prepareStatement(inc);
                psInc.setInt(1, eventID);
                psInc.executeUpdate();
            }

            c.commit();
            return affected == 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            if (c != null) try { c.rollback(); } catch (SQLException ignore) {}
        } finally {
            if (c != null) try { c.setAutoCommit(true); } catch (SQLException ignore) {}
            closeStatement(psInc);
            closeStatement(psIns);
            closeConnection(c);
        }
        return false;
    }

    @Override
    public boolean like(int eventID, String visitorID) { return reactToEvent(eventID, visitorID, 1); }

    @Override
    public boolean dislike(int eventID, String visitorID) { return reactToEvent(eventID, visitorID, -1); }

    private boolean reactToEvent(int eventID, String visitorID, int value) {
        String ins = "insert ignore into event_reactions(event_id, visitor_id, reaction, reacted_at) values(?, ?, ?, now())";
        String incLike = "update events set likes = likes + 1 where event_id = ?";
        String incDislike = "update events set dislikes = dislikes + 1 where event_id = ?";

        Connection c = null;
        PreparedStatement psIns = null, psInc = null;

        try {
            c = newConnection();
            c.setAutoCommit(false);

            psIns = c.prepareStatement(ins);
            psIns.setInt(1, eventID);
            psIns.setString(2, visitorID);
            psIns.setInt(3, value);
            int affected = psIns.executeUpdate();

            if (affected == 0) {
                c.rollback();
                return false;
            }

            psInc = c.prepareStatement(value > 0 ? incLike : incDislike);
            psInc.setInt(1, eventID);
            psInc.executeUpdate();

            c.commit();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            if (c != null) try {
                c.rollback();
            } catch (SQLException ignore) {
            }
        } finally {
            if (c != null) try {
                c.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
            closeStatement(psInc);
            closeStatement(psIns);
            closeConnection(c);
        }
        return false;
    }

    @Override
    public int capacityOf(int eventID) {
        String sql = "select coalesce(max_capacity, 0) from events where event_id = ?";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, eventID);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(c);
        }
        return 0;
    }

    @Override
    public boolean setStartAt(int eventID, LocalDateTime startAt) {
        String sql = "update events set start_at = ? where event_id = ?";
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(startAt));
            ps.setInt(2, eventID);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            closeStatement(ps);
            closeConnection(c);
        }
        return false;
    }

    private void upsertTagsAndBindings(Connection c, int eventID, List<String> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) return;

        String insTag = "insert into tags(tag_name) values (?) on duplicate key update tag_name = tag_name";
        String selTag = "select tag_id from tags where tag_name = ?";
        String bind   = "insert ignore into event_tags(event_id, tag_id) values (?, ?)";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            for (String raw : tags) {
                String t = normalize(raw);
                if (t == null || t.isBlank()) continue;

                ps = c.prepareStatement(insTag);
                ps.setString(1, t);
                ps.executeUpdate();
                closeStatement(ps); ps = null;

                int tagId = 0;
                ps = c.prepareStatement(selTag);
                ps.setString(1, t);
                rs = ps.executeQuery();
                if (rs.next()) tagId = rs.getInt(1);
                closeResultSet(rs); rs = null;
                closeStatement(ps); ps = null;

                ps = c.prepareStatement(bind);
                ps.setInt(1, eventID);
                ps.setInt(2, tagId);
                ps.executeUpdate();
                closeStatement(ps); ps = null;
            }
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }

    private String normalize(String tagName) {
        return tagName == null ? null : tagName.trim().toLowerCase();
    }
}
