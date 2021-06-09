package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTest {

//    public static void main(String[] args) throws Exception {
//         Connection connection = null;
//         Statement statement = null;
//         ResultSet resultSet = null;
//        try { // 6
//            connection = (new JdbcTest()).getConnection();
//            System.out.println("Connection is ready");
//
//            statement = connection.createStatement(); // 1
//            resultSet = statement.executeQuery("select *from payment limit 2;"); // 2
//
//            while (resultSet.next()) { // 3
//                System.out.println("["+ resultSet.getString(1) +"]" + "["+ resultSet.getString("id") +"]"); // 4
//                System.out.println("["+ resultSet.getString(1) +"]" + "["+ resultSet.getString("id") +"]"); // 4
//            }
//
//            connection.close();
//        } catch (SQLException ex) {
//            System.out.println("Connection Failed");
//        } finally {
//            try { if (resultSet != null) resultSet.close(); } catch (SQLException ex) { }
//            try { if (statement != null) statement.close(); } catch (SQLException ex) { }
//            try { if (connection != null) connection.close(); } catch (SQLException ex) { }
//        }
//    }

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        int affectedRowsCount = 0;
        try {
            connection = (new JdbcTest()).getConnection();
            connection.setAutoCommit(false); // (1)
            statement = connection.createStatement();

            affectedRowsCount = statement.executeUpdate("UPDATE payment SET amount = 1.00 WHERE id = -1;"); // (2)

            if(affectedRowsCount == 1) { // (3)
                System.out.println("변경 성공");
                connection.commit();
            }else{
                System.out.println("변경 실패");
                connection.rollback();
            }

            connection.close();
        } catch (SQLException ex) {
            System.out.println("Connection Failed");
        } finally {
            try { if (resultSet != null) resultSet.close(); } catch (SQLException ex) { }
            try { if (statement != null) statement.close(); } catch (SQLException ex) { }
            try { if (connection != null) connection.close(); } catch (SQLException ex) { }
        }
    }

    public Connection getConnection() throws Exception {
        final String driver = "com.mysql.cj.jdbc.Driver";
        final String url = "jdbc:mysql://localhost:3366/batch_study";
        final String user = "root";
        final String password = "";

        Class.forName(driver).newInstance();
        return DriverManager.getConnection(url, user, password);
    }
}
