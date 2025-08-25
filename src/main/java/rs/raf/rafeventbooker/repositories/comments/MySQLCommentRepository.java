package rs.raf.rafeventbooker.repositories.comments;

import rs.raf.rafeventbooker.model.Comment;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCommentRepository extends MySQLAbstractRepository implements CommentRepository {
    private Comment mapComment(ResultSet resultSet) throws SQLException {
        return new Comment(
                resultSet.getInt("comment_id"),
                resultSet.getInt("event_id"),
                resultSet.getString("author_name"),
                resultSet.getString("comment_content"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getInt("likes"),
                resultSet.getInt("dislikes")
        );
    }

    @Override
    public Optional<Comment> getCommentById(int commentID) {
        String sql = "select * from comments where comment_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, commentID);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(this.mapComment(resultSet));
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
    public Page<Comment> getCommentsForEvent(int eventID, int page, int size) {
        int checkSize = size <= 0 ? 20 : size;
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * checkSize;

        String count = "SELECT COUNT(*) FROM comments WHERE event_id=?";
        String data = "SELECT * FROM comments WHERE event_id=? ORDER BY created_at DESC LIMIT ? OFFSET ?";

        List<Comment> content = new ArrayList<>();
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(data);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setInt(2, checkSize);
            preparedStatement.setInt(3, offset);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                content.add(this.mapComment(resultSet));
            }

            preparedStatement = connection.prepareStatement(count);
            preparedStatement.setInt(1, eventID);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                total = resultSet.getInt(1);
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        int totalPages = (int) Math.ceil((double) total/checkSize);
        return new Page<>(content, safePage, checkSize, total, totalPages);
    }

    @Override
    public int createComment(Comment comment) {
        String sql="INSERT INTO comments(event_id, author_name, comment_content, created_at, likes, dislikes) VALUES(?,?,?,?,0,0)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, comment.getEventID());
            preparedStatement.setString(2, comment.getCommentAuthor());
            preparedStatement.setString(3, comment.getCommentContent());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
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
    public boolean deleteComment(int commentID) {
        String sql = "delete from comments where comment_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, commentID);

            return preparedStatement.executeUpdate() == 0;
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return false;
    }

    @Override
    public boolean like(int commentID, String visitorID) {
        return reactToComment(commentID, visitorID, 1);
    }

    @Override
    public boolean dislike(int commentID, String visitorID) {
        return reactToComment(commentID, visitorID, -1);
    }

    private boolean reactToComment(int commentID, String visitorID, int value) {
        String check = "select 1 from comment_reactions where comment_id = ? and visitor_id = ?";
        String insert = "insert into comment_reactions (comment_id, visitor_id, reaction, reacted_at) values (?, ?, ?, now())";
        String increase = "";
        if (value > 0) {
            increase = "update comments set likes = likes + 1 where comment_id = ?";
        } else {
            increase = "update comments set dislikes = dislikes + 1 where comment_id = ?";
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(check);
            preparedStatement.setInt(1, commentID);
            preparedStatement.setString(2, visitorID);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                connection.rollback();
                return false;
            }

            preparedStatement = connection.prepareStatement(insert);
            preparedStatement.setInt(1, commentID);
            preparedStatement.setString(2, visitorID);
            preparedStatement.setInt(3, value);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(increase);
            preparedStatement.setInt(1, commentID);
            preparedStatement.executeUpdate();
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
}
