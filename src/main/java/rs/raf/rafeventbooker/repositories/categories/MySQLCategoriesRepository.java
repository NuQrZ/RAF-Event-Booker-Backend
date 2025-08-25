package rs.raf.rafeventbooker.repositories.categories;

import rs.raf.rafeventbooker.model.Category;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCategoriesRepository extends MySQLAbstractRepository implements CategoriesRepository {
    private Category mapCategory(java.sql.ResultSet resultSet) throws SQLException {
        return new Category(
                resultSet.getInt("categoryID"),
                resultSet.getString("categoryName"),
                resultSet.getString("categoryDescription")
        );
    }

    @Override
    public Optional<Category> getCategoryByID(int categoryID) {
        String sql = "select * from categories where category_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,  categoryID);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapCategory(resultSet));
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
    public Optional<Category> getCategoryByName(String categoryName) {
        String sql = "select * from categories where category_name = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, categoryName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapCategory(resultSet));
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
    public Page<Category> getAllCategories(int page, int size) {
        int checkPage = Math.max(page, 0);
        int checkSize = size <= 0 ? 20 : size;
        int offset = checkPage * checkSize;

        String data = "select * from categories order by category_name limit ? offset ?";
        String count = "select count(*) from categories";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int total = 0;
        List<Category> content = new ArrayList<>();

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(data);
            preparedStatement.setInt(1, checkSize);
            preparedStatement.setInt(2, offset);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                content.add(mapCategory(resultSet));
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

    @Override
    public int createCategory(Category category) {
        String sql = "insert into categories(category_name, category_description) values (?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, category.getCategoryname());
            preparedStatement.setString(2, category.getCategoryDescription());
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
    public int updateCategory(Category category) {
        String sql = "update categories set category_name = ?, category_description = ? where category_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, category.getCategoryname());
            preparedStatement.setString(2, category.getCategoryDescription());
            preparedStatement.setInt(3, category.getCategoryID());

            return preparedStatement.executeUpdate();
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
    public boolean deleteCategory(int categoryID) {
        String delSql = "delete from categories where category_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(delSql);
            preparedStatement.setInt(1, categoryID);
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
    public boolean categoryExists(String categoryName) {
        String sql = "select 1 from categories where category_name = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, categoryName);

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
    public boolean hasEvents(int categoryID) {
        String sql = "select 1 from events where category_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = newConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, categoryID);

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
}
