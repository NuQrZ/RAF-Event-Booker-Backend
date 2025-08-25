package rs.raf.rafeventbooker.repositories.rsvp;

import rs.raf.rafeventbooker.model.RSVP;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.util.Optional;

public class MySQLRsvpRepository extends MySQLAbstractRepository implements RsvpRepository {
    private RSVP mapRSVP(ResultSet resultSet) throws SQLException {
        return new RSVP(
                resultSet.getInt("event_id"),
                resultSet.getString("user_email"),
                resultSet.getTimestamp("rsvp_at").toLocalDateTime()
        );
    }

    @Override
    public int createRsvp(RSVP rsvp) {
        String sql = "insert into rsvps (event_id, user_email, rsvp_at) values(?, ?, ?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, rsvp.getEventID());
            preparedStatement.setString(2, rsvp.getUserEmail());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(rsvp.getCreatedAt()));

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
    public boolean deleteRsvp(int eventID, String userEmail) {
        String sql = "delete from rsvps where event_id = ? and user_email = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setString(2, userEmail);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return false;
    }

    @Override
    public int countRSVPsByEvent(int eventID) {
        String sql = "select count(*) from rsvps where event_id = ?";
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
    public boolean rsvpExists(int eventID, String userEmail) {
        String sql = "select 1 from rsvps where event_id = ? and user_email = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setString(2, userEmail);

            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
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
    public Optional<RSVP> getRSVP(int eventID, String userEmail) {
        String sql = "select * from rsvps where event_id = ? and user_email = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, eventID);
            preparedStatement.setString(2, userEmail);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapRSVP(resultSet));
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
}
