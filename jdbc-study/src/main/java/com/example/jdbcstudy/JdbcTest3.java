package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcTest3 {

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = (new JdbcTest3()).getConnection();
            PreparedStatement statement1 = connection.prepareStatement("select *from payment where id = ?");
            PreparedStatement statement2 = connection.prepareStatement("select *from payment where id = ?");
            PreparedStatement statement3 = connection.prepareStatement("select *from payment where id = ?");

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
