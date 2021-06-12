package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTest5 {

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = (new JdbcTest()).getConnection();
            connection.setAutoCommit(false); // (1)
            statement = connection.createStatement();

            statement.executeUpdate("UPDATE payment SET amount = 1.00 WHERE id = 1;");
            statement.executeUpdate("UPDATE payment SET amount = 1.00 WHERE id = 2;");
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
        } finally {
            try { if (resultSet != null) resultSet.close(); } catch (SQLException ex) { }
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
