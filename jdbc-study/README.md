# JDBC
자바 프로그램 언어로 MySQL 데이터베이스에 접속해서 SQL을 실행하려면 자바에서 제공하는 표준 데이터베이스 접속 API인 JDBC를 이용 해야한다. 자바에서 제공하는 JDBC는 사실은 껍대기 Inerface일 뿐이며, 실제 각 DBMS에 접속해 필요한 작업을 하는 알맹이는 각 DBMS 제조사에서 제공하는 JDBC 드라이버다.

### MySQL Connector/J 를 이용한 개발

#### MySQL 서버 접속
Connector/J를 이용해 MySQL 서버에 접속하려면 JDBC URL 이라는 개념을 알아야 한다. 여기서 URL은 일반적으로 HTTP, FTP에서 사용하는 URL이 아니라 접속할 MySQL 서버의 정보를 표준 포맷으로 조합한 문자열이다. 때로는 이를 컨넥션 스트링이라고 표현하기도 한다. MySQL Connector/J를 이용해 MySQL ConntorJ를 이용해 MySQL 서버에 접속하는 예제를 보자.

```java
public class JdbcTest {

    public static void main(String[] args) throws Exception {
        final Connection con;
        try { // 6
            con = (new JdbcTest()).getConnection();
            System.out.println("Connection is ready");
            
            con.close(); // 5
        } catch (SQLException ex) { // 7
            System.out.println("Connection Failed");
        }
    }

    public Connection getConnection() throws Exception {
        final String driver = "com.mysql.cj.jdbc.Driver"; // 1
        final String url = "jdbc:mysql://localhost:3366/test_db"; // 2
        final String user = "root"; 
        final String password = "";

        Class.forName(driver).newInstance(); // 3
        return DriverManager.getConnection(url, user, password); // 4
    }
}
```

1. MySQL 서버 접속을 위해 JDBC URL을 설정
2. 현재는 없지만 성능이나 작동 방식을 변경하기 위해 Connctor/J에 별도의 옵션을 설정해야 할때 이 때는 ?를 표시히고 키/값 쌍으로 변수 값으로 사용
3. MySQL JDBC 드라이버 클래스의 로딩이 정상적으로 완료되면 DriverManager.getConnection() 명령어를 이용해 애플리케이션 MySQL
4. 자바에서 많은 자원이나 변수가 자동으로 소멸되지만 데이터베이스 컨넥션과 같은 네트워크 자원은 사용이 끄타면 즉시 해제하는 것이 좋다. 특히 데이터베이스 컨넥션과 같은 자원은 프로그램 코드에서 사용 직전에 가져와서, 사용이 완료됨과 동시에 바납하는 것이 좋다.
5. `con.close()` 명령어를 이용해 컨넥션을 종료 하고 있다.
6. `try-catch` 예외 처리를 한다. 오류가 발생하면 로깅이나 재처리 코드를 작성하는 것이 좋다
7. 애플리케이션을 재처리 과정을 구현할 때는 `getSQLState()`, `getErrorCode()` 함수를 이용해 지정된 에러 코드로 예외 상황을 판단하는 것이 좋다.


#### select 실행
```sql
public static void main(String[] args) throws Exception {
   Connection connection = null;
   Statement statement = null;
   ResultSet resultSet = null;
   try { // 6
      connection = (new JdbcTest()).getConnection();
      System.out.println("Connection is ready");

      statement = connection.createStatement(); // (1)
      resultSet = statement.executeQuery("select *from payment limit 2;"); // (2)

      while (resultSet.next()) { // (3)
            System.out.println("["+ resultSet.getString(1) +"]" + "["+ resultSet.getString("id") +"]"); // (4)
      }

      connection.close();
   } catch (SQLException ex) {
      System.out.println("Connection Failed");
   } finally {
      try { if (resultSet != null) resultSet.close(); } catch (SQLException ex) { }
      try { if (statement != null) statement.close(); } catch (SQLException ex) { }
      try { if (connection != null) connection.close(); } catch (SQLException ex) { }
   }
}
```
**(1) Statement 갹체는 JDBC를 사용하는 애플리케이션에서 모든 SQL문과 DDL 문장을 실행하는데 필요한 객체다.** 그리고 이와 비슷한 방식으로 사용하지만 프리페어 스테이트먼트를 실행할 때 사용하는 PreparedStatement 객체와 스토어드 프로시저를 실핼할 때 사용하는 CallableStatement 객체도 있다.

Statement 클래스는 `execute()`, `executeQuery()`, `excetueUpdate()`라는 세 가지 주요 함수를 제공한다. 결과 셋을 반환하 SELECT 쿠리 문장은 `executeQuery()` 함수를 사용하며, 결과 셋을 반환하지 않는 INSERT, UPDATE, DELETE, DDL 문장은 `executeUpdate()` 함수를 이용한다. 만약 실행 쿼리가 SELECT 인지 INSERT 인지 모를 때는 `exceute()` 함수를 이용할 수있다.

**(2)는 `excuteQuery()` 함수는 스토어는 프로시저에서 살펴본 커서와 거의 비슷한 기능을 제공하는 ResultSEt 이라는 객체를 반환한다. 즉시 SELECT 쿼리의 결과를 렠드 단위로 하나씩 페치할 수 있는 기능을 제공하는 객체다.**

ResultSet의 `next()` 함수는 결과 셋에 아직 읽지 않은 레코드가 더 있는지 확인할 수 있게 해준다. (3)만약 아직 읽지 않은 레코드가 남아 있다면 ResultSet의 `getString()`, `getInt()` 등의 함수를 이용해 칼럼 값을 가져올 수 있다. 칼럼 이름이나 SELECT 절에 나열된 칼럼의 순번을 인자로 해서 `getString()`, `getInt()` 등의 함수로 칼럼 값을 가져올 수 있다

#### INSERT/UPDATE/DELETE 실행

**SELECT 쿼리와는 달리 INSERT, UPDATE, DELETE 문장은 별도의 결과 셋을 반환하지 않으므로 `Statement.executeQuery()`함수 대신 `Statement.executeUpdate()` 함수를 사용해서 실행한다.** DDL이나 MySQL의 SET 명령과 같이 결과 셋을 변환하지 않는 SQL 명령으 모두 `excecuteUpdate()` 함수를 사용해 실행할 수 있다.

`excecuteUpdate()` 함수는 INSERT, UPDATE, DELETE 문장에 의해 변경된 레코드 건수를 반환한다. `excecuteUpdate()` 함수의 반환값은 별도로 확인하지 않고 무시해버릴 때가 많다. 하지만 실제로 DELETE 쿼리로 단 한 건만 삭제돼야 하는데, 한 건도 삭제되지 않았거나 두 건 이상의 레코드가 삭제됐다면 어떻게 해야할까? 만약 이런 상황ㅇ이 문제가 될 수지가 있다면 변경된 레코드 건수를 체크해서 COMMIT이나 ROLLBACK을 수행하게 해주는 것이 좋다.

```java
public static void main(String[] args) throws Exception {
   Connection connection = null;
   Statement statement = null;
   ResultSet resultSet = null;
   int affectedRowsCount = 0;
   try {
      connection = (new JdbcTest()).getConnection();
      connection.setAutoCommit(false); // (1)
      statement = connection.createStatement();

      affectedRowsCount = statement.executeUpdate("UPDATE payment SET amount = 1.00 WHERE id = -1;"); // (2)

      if(affectedRowsCount == 1) { // (3)
            System.out.println("변경 성공");
            connection.commit();
      }else{
            System.out.println("변경 실패");
            connection.rollback();
      }

      connection.close();
   } catch (SQLException ex) {
      System.out.println("Connection Failed");
   } finally {
      try { if (resultSet != null) resultSet.close(); } catch (SQLException ex) { }
      try { if (statement != null) statement.close(); } catch (SQLException ex) { }
      try { if (connection != null) connection.close(); } catch (SQLException ex) { }
   }
}
```
`connection.setAutoCommit(false);` 함수를 먼저 호출 했다. **MySQL에서는 매 쿼리가 정상적으로 실행되면 자동으로 트랜잭션 COMMIT된다. 이를 AutoCommit 이라고 표현하는데, 별도로 AutoCommit 모드를 변경하지 않았다면 이것이 기본 작동 모드다. 만약 하나의 트랜잭션으로 여러 개의 UPDATE, DELETE 문장을 묶어서 실행하려면 AutoCommit 모드를 FALSE로 설정해야 한다.** `connection.setAutoCommit(false);` 명령은 MySQL 서버가매 쿼리마다 자동으로 COMMIT을 실행하지 않도록 AutoCommit 모드를 FALSE로 변경하는 것이다.

`executeUpdate()` 실행하고 UPDATE 문장의 실행으로 변경된 레코드 건수를 affectedRowsCount 변수에 할당한다. 위 문장은 PK 값으로 변경하기 때문에 반드시 한 검만 변경됐는지 체크하기 위해 affectedRowsCount에 할단된 값이 1인지 비교해서 최종적으로 UPDATE 작업을 COMMIT할지 ROLLBACK할지 결정한다.

**만약 변경하려는 작업이 기존 값과 똑같다면 실질적인 변경 작업을 생략해버린다.**

#### Statement와 PreparedStatement의 차이