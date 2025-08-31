package rs.raf.rafeventbooker.repositories.comments;

import rs.raf.rafeventbooker.model.Comment;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCommentRepository extends MySQLAbstractRepository implements CommentRepository {

    private Comment mapComment(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getInt("comment_id"),
                rs.getInt("event_id"),
                rs.getString("author_name"),
                rs.getString("comment_content"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getInt("likes"),
                rs.getInt("dislikes")
        );
    }

    @Override
    public Optional<Comment> getCommentById(int commentID) {
        String sql = "select comment_id, event_id, author_name, comment_content, created_at, likes, dislikes from comments where comment_id = ?";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, commentID);
            rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapComment(rs));
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
    public Page<Comment> getCommentsForEvent(int eventID, int page, int size) {
        int s = size <= 0 ? 20 : size;
        int p = Math.max(1, page);
        int offset = (p - 1) * s;

        String dataSql  = "select comment_id, event_id, author_name, comment_content, created_at, likes, dislikes " +
                "from comments where event_id=? order by created_at desc limit ? offset ?";
        String countSql = "select count(*) from comments where event_id=?";

        List<Comment> content = new ArrayList<>();
        int total = 0;

        Connection c = null;
        PreparedStatement psData = null, psCount = null;
        ResultSet rsData = null, rsCount = null;

        try {
            c = newConnection();

            psData = c.prepareStatement(dataSql);
            psData.setInt(1, eventID);
            psData.setInt(2, s);
            psData.setInt(3, offset);
            rsData = psData.executeQuery();
            while (rsData.next()) content.add(mapComment(rsData));

            psCount = c.prepareStatement(countSql);
            psCount.setInt(1, eventID);
            rsCount = psCount.executeQuery();
            if (rsCount.next()) total = rsCount.getInt(1);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rsData);
            this.closeStatement(psData);
            this.closeResultSet(rsCount);
            this.closeStatement(psCount);
            this.closeConnection(c);
        }

        int totalPages = (int)Math.ceil((double)total / s);
        return new Page<>(content, p, s, total, totalPages);
    }

    @Override
    public int createComment(Comment comment) {
        String sql = "insert into comments(event_id, author_name, comment_content, created_at, likes, dislikes) " +
                "values(?, ?, ?, ?, 0, 0)";

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, comment.getEventID());
            ps.setString(2, comment.getCommentAuthor());
            ps.setString(3, comment.getCommentContent());
            ps.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
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
    public boolean updateCommentContent(int commentID, String content) {
        String sql = "update comments set comment_content = ? where comment_id = ?";
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setString(1, content);
            ps.setInt(2, commentID);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeStatement(ps);
            this.closeConnection(c);
        }
        return false;
    }

    @Override
    public boolean deleteComment(int commentID) {
        String sql = "delete from comments where comment_id = ?";
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = newConnection();
            ps = c.prepareStatement(sql);
            ps.setInt(1, commentID);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeStatement(ps);
            this.closeConnection(c);
        }
        return false;
    }

    @Override
    public boolean like(int commentID, String visitorID) { return reactToComment(commentID, visitorID, 1); }

    @Override
    public boolean dislike(int commentID, String visitorID) { return reactToComment(commentID, visitorID, -1); }

    private boolean reactToComment(int commentID, String visitorID, int value) {
        String insert = "insert ignore into comment_reactions(comment_id, visitor_id, reaction, reacted_at) values(?, ?, ?, now())";
        String incLike = "update comments set likes = likes + 1 where comment_id = ?";
        String incDislike = "update comments set dislikes = dislikes + 1 where comment_id = ?";

        Connection c = null;
        PreparedStatement psIns = null, psInc = null;

        try {
            c = newConnection();
            c.setAutoCommit(false);

            psIns = c.prepareStatement(insert);
            psIns.setInt(1, commentID);
            psIns.setString(2, visitorID);
            psIns.setInt(3, value);
            int affected = psIns.executeUpdate();

            if (affected == 0) {
                c.rollback();
                return false;
            }

            psInc = c.prepareStatement(value > 0 ? incLike : incDislike);
            psInc.setInt(1, commentID);
            psInc.executeUpdate();

            c.commit();
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            if (c != null) {
                try { c.rollback(); } catch (SQLException ignore) {}
            }
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (SQLException ignore) {}
            }
            this.closeStatement(psInc);
            this.closeStatement(psIns);
            this.closeConnection(c);
        }
        return false;
    }
}
