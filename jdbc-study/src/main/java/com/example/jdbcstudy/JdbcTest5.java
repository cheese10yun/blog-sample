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
            statement.execute("BEGIN");

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


//class Foo {
//    fun doSomething() {
//        try {
//            val connection = getConntetion()...
//            val statement = connection.createStatement()...
//            // 트랜잭션 1 새로 시작
//            statement.execute("BEGIN")
//            statement.executeUpdate("UPDATE payment SET amount = 1.00 WHERE id = 1;");
//
//            try {
//                // 트랜잭션 1 진행 일시 정지
//                val connection = getConntetion()...
//                val statement = connction.createStatement()...
//                // 트랜잭션 2 트랜잭션 새로 시작
//                statement.execute("BEGIN")
//                statement.executeUpdate("UPDATE member SET username = 'test' WHERE id = 1;");
//                // 트랜잭션 2 종료
//                connection.commit()
//
//            }catch (e: Exception){
//                // 트랜잭션 2 예외발생시 롤백, 트랜잭션 1은 try catch로 분리되어 있기 때문에 영향 없음
//                connection.rollback()
//            }
//            // 트랜잭션 1 종료
//            connection.commit();
//        } catch (e: Exception) {
//            connection.rollback()
//        }
//    }
//}