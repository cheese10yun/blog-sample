package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTest2 {

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = (new JdbcTest2()).getConnection();
            statement = connection.prepareStatement("select *from payment where id = ?"); // (1)
            statement.setInt(1, 1);
            resultSet = statement.executeQuery();
            if(resultSet.next()){
                System.out.println("id: " +resultSet.getString("id"));
            }
            resultSet.close();

            statement.setInt(1, 2);
            resultSet = statement.executeQuery();
            if(resultSet.next()){
                System.out.println("id: " +resultSet.getString("id"));
            }
            resultSet.close();
            resultSet = null;

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Connection Failed");
            System.out.printf(ex.getMessage());
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
