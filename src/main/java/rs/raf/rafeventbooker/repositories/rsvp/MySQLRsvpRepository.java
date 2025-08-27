package rs.raf.rafeventbooker.repositories.rsvp;

import rs.raf.rafeventbooker.model.RSVP;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.util.Optional;

public class MySQLRsvpRepository extends MySQLAbstractRepository implements RsvpRepository {

    private RSVP mapRSVP(ResultSet rs) throws SQLException {
        return new RSVP(
                rs.getInt("event_id"),
                rs.getString("user_email"),
                rs.getTimestamp("rsvp_at").toLocalDateTime()
        );
    }

    @Override
    public int createRsvp(RSVP rsvp) {
        String sql = "insert ignore into rsvps (event_id, user_email, rsvp_at) values (?, ?, ?)";
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, rsvp.getEventID());
            ps.setString(2, rsvp.getUserEmail());
            ps.setTimestamp(3, Timestamp.valueOf(rsvp.getCreatedAt()));

            int affected = ps.executeUpdate();
            return affected;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return -1;
    }

    @Override
    public boolean deleteRsvp(int eventID, String userEmail) {
        String sql = "delete from rsvps where event_id = ? and user_email = ?";
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, eventID);
            ps.setString(2, userEmail);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return false;
    }

    @Override
    public int countRSVPsByEvent(int eventID) {
        String sql = "select count(*) from rsvps where event_id = ?";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, eventID);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return 0;
    }

    @Override
    public boolean rsvpExists(int eventID, String userEmail) {
        String sql = "select 1 from rsvps where event_id = ? and user_email = ? limit 1";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, eventID);
            ps.setString(2, userEmail);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return false;
    }

    @Override
    public Optional<RSVP> getRSVP(int eventID, String userEmail) {
        String sql = "select event_id, user_email, rsvp_at from rsvps where event_id = ? and user_email = ? limit 1";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, eventID);
            ps.setString(2, userEmail);
            rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRSVP(rs));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return Optional.empty();
    }
}
