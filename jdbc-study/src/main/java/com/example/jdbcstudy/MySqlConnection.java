package com.example.jdbcstudy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class MySqlConnection {

    public static Connection getConnection(
        final HashMap<String, String> map
    ) throws Exception {


        final String driver = "com.mysql.cj.jdbc.Driver";
        final String url = "jdbc:mysql://localhost:3366/batch_study?useCursorFetch=true&defaultFetchSize=10000";
        final String user = "root";
        final String password = "";



        Class.forName(driver).newInstance();
        return DriverManager.getConnection(url, user, password);
    }
}
