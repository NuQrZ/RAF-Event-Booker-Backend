package rs.raf.rafeventbooker.repositories.user;

import org.mindrot.jbcrypt.BCrypt;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.model.User;
import rs.raf.rafeventbooker.model.enums.UserRole;
import rs.raf.rafeventbooker.model.enums.UserStatus;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLUserRepository extends MySQLAbstractRepository implements UserRepository {
    private User getUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserID(resultSet.getInt("user_id"));
        user.setEmail(resultSet.getString("user_email"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setPassword(resultSet.getString("user_password"));
        user.setUserRole(UserRole.valueOf(resultSet.getString("user_role")));
        user.setUserStatus(UserStatus.valueOf(resultSet.getString("user_status")));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        return user;
    }

    @Override
    public Optional<User> getUserByID(int userID) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userID);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(getUser(resultSet));
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
    public Optional<User> getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE user_email = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(getUser(resultSet));
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
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE user_email = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
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
    public boolean userExists(int userID) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userID);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
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
    public int createUser(User user) {
        String sql = "INSERT INTO users(user_email, first_name, last_name, user_password, user_role, user_status) VALUES (?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            String passwordHash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
            preparedStatement.setString(4, passwordHash);
            preparedStatement.setString(5, user.getUserRole().name());
            preparedStatement.setString(6, user.getUserStatus().name());
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
    public int updateUser(User user) {
        String sql = "UPDATE users SET user_email = ?, first_name = ?, last_name = ?, user_password = ?, user_role = ?, user_status = ? WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            String passwordHash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
            preparedStatement.setString(4, passwordHash);
            preparedStatement.setString(5, user.getUserRole().name());
            preparedStatement.setString(6, user.getUserStatus().name());
            preparedStatement.setInt(7, user.getUserID());

            return preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return -1;
    }

    @Override
    public int updateStatus(int userID, UserStatus userStatus) {
        String sql = "UPDATE users SET user_status = ? WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userStatus.name());
            preparedStatement.setInt(2, userID);

            return preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
        return -1;
    }

    @Override
    public boolean deleteUser(int userID) {
        String delSql = "DELETE FROM users WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();

            preparedStatement = connection.prepareStatement(delSql);
            preparedStatement.setInt(1, userID);
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
    public Page<User> getAllUsers(int page, int size) {
        int checkPage = Math.max(page, 0);
        int checkSize = size <= 0 ? 20 : size;
        int offset = checkPage * checkSize;

        String data = "SELECT * FROM users ORDER BY created_at DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) FROM users";
        List<User> content = new ArrayList<>();
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
                content.add(getUser(resultSet));
            }

            preparedStatement = connection.prepareStatement(count);
            resultSet = preparedStatement.executeQuery();

            resultSet.next();
            total = resultSet.getInt(1);
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        int totalPages = (int) Math.ceil((double)total / checkSize);
        return new Page<>(content, checkPage, checkSize, total, totalPages);
    }
}