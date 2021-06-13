package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.springframework.util.StopWatch;

public class JdbcCursorFetch {

    public static void main(String[] args) throws Exception {
        final Connection connection = (new JdbcCursorFetch()).getConnection();

        // 대용량 쿼리르 실행하는 부분
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final String sql = "SELECT *FROM payment WHERE created_at >= '2021-05-01 00:00:00' ORDER BY id DESC";
        final Statement statement = connection.prepareStatement(sql);
        final ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            System.out.println("id: " + resultSet.getString("id"));
        }

        resultSet.close();
        statement.close();
        connection.close();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());
    }

    public Connection getConnection() throws Exception {
        final String driver = "com.mysql.cj.jdbc.Driver";
        final String url = "jdbc:mysql://localhost:3366/batch_study?useCursorFetch=true&defaultFetchSize=10000";
        final String user = "root";
        final String password = "";

        Class.forName(driver).newInstance();
        return DriverManager.getConnection(url, user, password);
    }
}
// 62
// 62
// 63