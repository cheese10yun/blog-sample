package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StopWatch;

public class JdbcResultSetStreaming {

    public static void main(String[] args) throws Exception {
        final Connection connection = (new JdbcResultSetStreaming()).getConnection();
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final String sql = "SELECT *FROM payment WHERE created_at >= '2021-05-01 00:00:00' ORDER BY id DESC";
        final Statement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        statement.setFetchSize(Integer.MIN_VALUE);
        final ResultSet resultSet = statement.executeQuery(sql);

        final ArrayList<String> strings = new ArrayList<>();

        while (resultSet.next()) {
            final String id = resultSet.getString("id");
//            System.out.println("id: " + resultSet.getString("id"));
        }

        resultSet.close();
        statement.close();
        connection.close();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());
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

// 30
// 27
// 28
// 26
// 90.564387577
// 87.763164576