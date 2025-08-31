package rs.raf.rafeventbooker.repositories.categories;

import rs.raf.rafeventbooker.model.Category;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.repositories.MySQLAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCategoriesRepository extends MySQLAbstractRepository implements CategoriesRepository {

    private Category mapCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("category_description")
        );
    }

    @Override
    public Optional<Category> getCategoryByID(int categoryID) {
        String sql = "select category_id, category_name, category_description from categories where category_id = ?";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, categoryID);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Category> getCategoryByName(String categoryName) {
        String sql = "select category_id, category_name, category_description from categories where category_name = ?";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, categoryName);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return Optional.empty();
    }

    @Override
    public Page<Category> getAllCategories(int page, int size) {
        int p = Math.max(page, 1);                 // 1-based
        int s = size <= 0 ? 20 : size;
        int offset = (p - 1) * s;

        String dataSql  = "select category_id, category_name, category_description from categories order by category_name limit ? offset ?";
        String countSql = "select count(*) from categories";

        Connection connection = null;
        PreparedStatement psData = null;
        PreparedStatement psCount = null;
        ResultSet rsData = null;
        ResultSet rsCount = null;

        List<Category> content = new ArrayList<>();
        int total = 0;

        try {
            connection = newConnection();

            psData = connection.prepareStatement(dataSql);
            psData.setInt(1, s);
            psData.setInt(2, offset);
            rsData = psData.executeQuery();
            while (rsData.next()) {
                content.add(mapCategory(rsData));
            }

            psCount = connection.prepareStatement(countSql);
            rsCount = psCount.executeQuery();
            if (rsCount.next()) {
                total = rsCount.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(rsData);
            this.closeStatement(psData);
            this.closeResultSet(rsCount);
            this.closeStatement(psCount);
            this.closeConnection(connection);
        }

        int totalPages = (int) Math.ceil((double) total / s);
        return new Page<>(content, p, s, total, totalPages);
    }

    @Override
    public int createCategory(Category category) {
        String sql = "insert into categories(category_name, category_description) values (?, ?)";

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet keys = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getCategoryDescription());
            ps.executeUpdate();

            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeResultSet(keys);
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return -1;
    }

    @Override
    public int updateCategory(Category category) {
        String sql = "update categories set category_name = ?, category_description = ? where category_id = ?";

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getCategoryDescription());
            ps.setInt(3, category.getCategoryID());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            this.closeStatement(ps);
            this.closeConnection(connection);
        }
        return -1;
    }

    @Override
    public boolean deleteCategory(int categoryID) {
        String delSql = "delete from categories where category_id = ?";

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(delSql);
            ps.setInt(1, categoryID);
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
    public boolean categoryExists(String categoryName) {
        String sql = "select 1 from categories where category_name = ? limit 1";

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, categoryName);
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
    public boolean hasEvents(int categoryID) {
        String sql = "select 1 from events where category_id = ? limit 1";

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = newConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, categoryID);
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
}
