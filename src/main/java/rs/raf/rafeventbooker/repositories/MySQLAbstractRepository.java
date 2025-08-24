package rs.raf.rafeventbooker.repositories;

import java.sql.*;

public class MySQLAbstractRepository {
     public MySQLAbstractRepository() {
         try {
             Class.forName("com.mysql.cj.jdbc.Driver");
         } catch (ClassNotFoundException classNotFoundException) {
             System.err.println(classNotFoundException.getMessage());
         }
     }

     protected Connection newConnection() throws SQLException {
         return DriverManager.getConnection(
                 "jdbc:mysql://" + this.getHost() + ":" + this.getPort() + "/" + this.getDatabase(), this.getUserName(), this.getPassword()
         );
     }

     protected String getHost() {
         return "localhost";
     }

     protected int getPort() {
         return 3306;
     }

     protected String getDatabase() {
         return "raf_event_booker";
     }

     protected String getUserName() {
         return "root";
     }

     protected String getPassword() {
         return "GojxSQL2003";
     }

     protected void closeResultSet(ResultSet resultSet) {
         if (resultSet != null) {
             try {
                 resultSet.close();
             } catch (SQLException sqlException) {
                 System.err.println(sqlException.getMessage());
             }
         }
     }

     protected void closeStatement(Statement statement) {
         if (statement != null) {
             try {
                 statement.close();
             } catch (SQLException sqlException) {
                 System.err.println(sqlException.getMessage());
             }
         }
     }

     protected void closeConnection(Connection connection) {
         if (connection != null) {
             try {
                 connection.close();
             } catch (SQLException sqlException) {
                 System.err.println(sqlException.getMessage());
             }
         }
     }
}
