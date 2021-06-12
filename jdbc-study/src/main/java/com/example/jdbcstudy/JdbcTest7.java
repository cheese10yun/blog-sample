package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcTest7 {

    public static void main(String[] args) throws Exception {
        final Connection connection = (new JdbcTest()).getConnection();
        final Statement statement = connection.createStatement(
            ResultSet.TYPE_FORWARD_ONLY, // (1)
            ResultSet.CONCUR_READ_ONLY // (2)
        );
        statement.setFetchSize(Integer.MIN_VALUE);
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
