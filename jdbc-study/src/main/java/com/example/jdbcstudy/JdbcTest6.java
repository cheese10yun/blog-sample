package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTest6 {

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = (new JdbcTest()).getConnection();

            statement.setFetchSize(Integer.MIN_VALUE);

            statement.execute("BEGIN");
            statement = connection.createStatement();

            statement.executeUpdate("UPDATE payment SET amount = 1.00 WHERE id = 1;");
            statement.executeUpdate("UPDATE payment SET amount = 1.00 WHERE id = 2;");
            connection.commit(); // 위 두 쿼리의 변경 내용을 영구히 적용
        } catch (SQLException ex) {
            connection.rollback();
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
