package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class JdbcTest4 {

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = (new JdbcTest4()).getConnection();
            statement = connection.prepareStatement("INSERT INTO payment (amount, order_id, created_at, updated_at) VALUES (?, ?, now(), now())");

            for (int i = 0; i < 10; i++){
                statement.setLong(1, 1);
                statement.setLong(2, 1);
                statement.addBatch();
            }
            statement.executeBatch();

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Connection Failed");
            System.out.println(ex.getMessage());
        } finally {
            try { if (statement != null) statement.close(); } catch (SQLException ex) { }
            try { if (connection != null) connection.close(); } catch (SQLException ex) { }
        }
    }

    public Connection getConnection() throws Exception {
        final String driver = "com.mysql.cj.jdbc.Driver";
        final String url = "jdbc:mysql://localhost:3366/batch_study?rewriteBatchedStatements=true&useServerPrepStmts=false";
        final String user = "root";
        final String password = "";

        Class.forName(driver).newInstance();
        return DriverManager.getConnection(url, user, password);
    }
}
