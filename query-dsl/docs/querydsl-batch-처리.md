# QueryDSL-SQL로 대량 데이터 Batch Insert / Update 성능 개선하기

## 들어가며 — JPA 대량 처리의 한계

수만 건의 데이터를 한 번에 저장하거나 수정하는 배치 작업을 JPA로 그대로 구현하면, 어느 순간부터 처리 시간이 예상을 한참 벗어나기 시작합니다. JPA로 대량의 데이터를 저장하거나 수정해야 할 때는 각각 다른 이유로 성능 문제에 부딪힙니다. Insert는 `saveAll`을 사용할 때 `IDENTITY` 전략이 JDBC 레벨의 Batch Insert를 막아버리는 문제가 있고, Update는 Dirty Checking 방식이 엔티티 수만큼 개별 UPDATE 쿼리를 만들어내는 문제가 있습니다. 원인은 다르지만 결과는 같습니다. 데이터 건수만큼 쿼리가 반복 전송되고, 데이터 양이 늘어날수록 성능이 급격히 저하됩니다.

이 글에서는 두 문제를 QueryDSL-SQL의 `SQLQueryFactory`와 `addBatch`로 각각 해결하는 방법을 다룹니다. 먼저 두 작업에 공통으로 쓰이는 개념을 정리하고, Batch Insert와 Batch Update를 차례로 적용한 뒤, 각각 실제로 측정한 성능 결과까지 살펴봅니다.

## 공통 토대: QueryDSL-SQL 준비

### 왜 QueryDSL-SQL인가 (추가 라이브러리 없이 Type-Safe)

대량의 데이터를 다뤄야 할 때 흔히 떠올리는 대안 중 하나는 Exposed 같은 별도의 SQL 전용 라이브러리를 도입하는 것입니다. 하지만 오직 대량 처리 성능 개선만을 위해 JPA 환경에 새로운 ORM을 도입하고 혼합해서 사용하는 것은 설정의 복잡함과 학습 곡선 측면에서 비효율적일 수 있습니다.

이미 프로젝트에서 JPA와 QueryDSL을 사용하고 있다면, 추가적인 라이브러리 도입 없이 **QueryDSL-SQL** 모듈만으로 Type-Safe하게 대량 처리를 구현할 수 있습니다. QueryDSL-SQL은 JPA 엔티티 모델이 아닌 데이터베이스 스키마를 기반으로 쿼리를 작성할 수 있게 해주는 모듈로, JPA가 제공하지 않는 세밀한 SQL 제어(Batch Insert/Update, 특정 벤더 전용 구문 등)가 필요할 때 유용합니다. `SQLQueryFactory`를 통해 JDBC 레벨의 기능을 Type-Safe하게 사용할 수 있습니다.

QueryDSL-SQL을 사용하기 위해 `build.gradle.kts`에 아래 의존성을 추가합니다.

```kotlin
dependencies {
    // QueryDSL JPA (기본 사용)
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")

    // QueryDSL SQL (Batch Insert/Update를 위해 필요)
    implementation("com.querydsl:querydsl-sql:5.1.0")
}
```

### 핵심 개념: RelationalPathBase 와 addBatch()

`SQLQueryFactory`는 `RelationalPath` 타입을 요구합니다. 그런데 JPA 엔티티 기반으로 생성되는 `QWriter`는 `EntityPathBase`이기 때문에, 테이블 참조로 직접 사용할 수 없습니다. 그래서 `RelationalPathBase`로 테이블 메타데이터를 별도로 정의해야 합니다. 컬럼 참조 자체는 기존에 생성된 `QWriter`의 path를 그대로 활용할 수 있습니다.

* **RelationalPathBase**: SQL 쿼리 작성을 위해 대상 테이블의 메타데이터를 정의합니다.
* **QWriter.writer.\***: 컬럼 참조에는 기존에 생성된 Q클래스의 path를 그대로 활용합니다.
* **addBatch()**: 루프를 돌며 데이터를 즉시 실행하지 않고, JDBC의 Batch 기능을 활용하기 위해 메모리에 쿼리 파라미터들을 쌓아둡니다.
* **execute()**: 쌓여있는 Batch 쿼리를 데이터베이스로 한 번에 전송하여 실행합니다.

Insert와 Update 모두 이 `addBatch` + `execute()` 패턴을 동일하게 사용합니다. 다만 `addBatch()`를 호출하기 전에 어떤 조건이 필요한지는 두 작업이 다릅니다 — 이 차이는 뒤에서 각 챕터를 다룰 때 짚어보겠습니다.

### 성능 측정 방법론 (iterations=5, 첫 회 warm-up 제외, 평균 산출)

이후 Insert, Update 각 챕터에서 제시하는 성능 측정 결과는 모두 아래와 같은 공통 방법론으로 산출되었습니다.

* **반복 측정**: 각 데이터 구간(100건 ~ 10,000건)마다 총 **5회** 반복하여 측정했습니다.
* **Warm-up 고려**: 테스트 실행 시 **첫 번째 회차는 결과에서 제외**했습니다. 데이터베이스 커넥션 풀에서 커넥션을 처음 생성하는 초기 비용 등이 포함되어 결과가 왜곡되는 것을 방지하기 위함입니다.
* **평균값 산출**: 첫 회차를 제외한 나머지 **4회의 실행 시간**을 합산하여 평균값을 산출함으로써 보다 신뢰성 있는 성능 데이터를 얻었습니다.

이 절에서는 방법론만 다룹니다. 구체적인 수치와 개선율은 각 챕터의 "성능 측정 결과"에서 제시합니다.

이제 이 공통 토대 위에서, 먼저 Batch Insert부터 적용해보겠습니다.

## Batch Insert 적용

### 의존성 & 초기화: SQLQueryFactory 구성

`BatchInsertService`는 `JPAQueryFactory`, `JdbcTemplate`, `WriterRepository`, `DataSource` 총 4개의 의존성을 주입받습니다. 이 중 Batch Insert/Update에 실제로 사용되는 것은 `dataSource`입니다.

```kotlin
@Service
class BatchInsertService(
    private val jpaQueryFactory: JPAQueryFactory,
    private val jdbcTemplate: JdbcTemplate,
    private val writerRepository: WriterRepository,
    private val dataSource: DataSource
) {

    private val sqlQueryFactory: SQLQueryFactory by lazy {
        SQLQueryFactory(Configuration(MySQLTemplates()), dataSource)
    }

    // ...
}
```

`sqlQueryFactory`는 메서드가 호출될 때마다 새로 만들지 않고, `by lazy`로 최초 한 번만 생성한 뒤 클래스 내에서 공유하는 인스턴스로 사용합니다. Insert와 Update 양쪽 메서드가 이 공유 인스턴스를 그대로 재사용합니다.

### 구현: executeBulkInsertWritersWithSql (name/email/score/reputation/active 5필드)

```kotlin
@Transactional
fun executeBulkInsertWritersWithSql(writers: List<Writer>): Long {
    // 1. 테이블 메타데이터 정의
    val writerTable = RelationalPathBase(Writer::class.java, "writer", null, "writer")
    // 2. SQLQueryFactory (공유 인스턴스 사용)
    val insert = sqlQueryFactory.insert(writerTable)
    // 3. 데이터를 Batch에 추가
    for (writer in writers) {
        insert.set(QWriter.writer.name, writer.name)
        insert.set(QWriter.writer.email, writer.email)
        insert.set(QWriter.writer.score, 1)
        insert.set(QWriter.writer.reputation, 1.toDouble())
        insert.set(QWriter.writer.active, true)
        insert.addBatch() // 메모리에 쿼리 적재
    }

    // 4. 일괄 실행
    return insert.execute()
}
```

`name`, `email`은 인자로 받은 `writer`의 값을 그대로 세팅하고, `score`, `reputation`, `active`는 초기값으로 고정해서 세팅합니다. Insert에는 Update와 달리 `where` 조건이 필요 없습니다 — 새로운 row를 추가하는 작업이기 때문에 대상을 특정할 필요가 없습니다.

### rewriteBatchedStatements=true 의 효과

MySQL을 사용하는 경우, JDBC 연결 URL에 `rewriteBatchedStatements=true` 옵션을 반드시 추가해야 합니다.

```
jdbc:mysql://localhost:3306/mydb?rewriteBatchedStatements=true
```

**이 옵션이 필요한 이유:**
기본적으로 MySQL JDBC 드라이버는 `addBatch()`로 들어온 쿼리들을 개별적인 Insert 구문으로 전송합니다. 하지만 이 옵션을 활성화하면 드라이버 레벨에서 여러 개의 Insert 구문을 하나의 `INSERT INTO ... VALUES (...), (...), (...)` 형태의 **Multi-Value Insert** 구문으로 재작성(Rewrite)하여 전송합니다. 이를 통해 네트워크 패킷 수를 획기적으로 줄이고 데이터베이스의 파싱 비용을 절감하여 성능을 극대화할 수 있습니다.

**처리 흐름:**

```
addBatch() 호출 (여러 번)
    ↓
execute() 호출
    ↓
MySQL JDBC 드라이버가 rewriteBatchedStatements=true 감지
    ↓
개별 INSERT들을 Multi-Value INSERT로 재작성
    ↓
INSERT INTO writer (name, email) VALUES ('a','a@a.com'), ('b','b@b.com'), ('c','c@c.com')
    ↓
DB로 전송
```

**옵션 유무에 따른 차이:**

| 상황                                             | DB로 전달되는 SQL                         |
|:-----------------------------------------------|:-------------------------------------|
| `addBatch()` 단독                                | `INSERT ... VALUES (a)` × N번         |
| `addBatch()` + `rewriteBatchedStatements=true` | `INSERT ... VALUES (a),(b),(c)` × 1번 |

`rewriteBatchedStatements=true`는 애플리케이션 코드 변경 없이 JDBC 드라이버 레벨에서 자동으로 변환해주므로, 옵션 하나만으로 Multi-Value INSERT의 성능 이점을 얻을 수 있습니다.

**실제 전송 쿼리 확인: profileSQL=true**

실제로 DB에 어떤 쿼리가 전송되는지 확인하려면 JDBC URL에 `profileSQL=true` 옵션을 추가합니다.

```
jdbc:mysql://localhost:3306/mydb?rewriteBatchedStatements=true&profileSQL=true
```

**`rewriteBatchedStatements=true` 일 때** — 10건이 하나의 Multi-Value INSERT로 전송됩니다.

```
[QUERY] insert into writer (name, email, score, reputation, active)
values ('name-5-1', 'email-5-1', 1, 1.0, 1),('name-5-2', 'email-5-2', 1, 1.0, 1), ... ,('name-5-10', 'email-5-10', 1, 1.0, 1)
```

**`rewriteBatchedStatements=false` 일 때** — 건별로 개별 INSERT가 반복 전송됩니다.

```
[QUERY] insert into writer (name, email, score, reputation, active) values ('name-5-6', 'email-5-6', 1, 1.0, 1)
[QUERY] insert into writer (name, email, score, reputation, active) values ('name-5-7', 'email-5-7', 1, 1.0, 1)
[QUERY] insert into writer (name, email, score, reputation, active) values ('name-5-8', 'email-5-8', 1, 1.0, 1)
...
```

`profileSQL=true`는 개발/테스트 환경에서 실제 전송 쿼리를 눈으로 검증할 때 유용하며, 운영 환경에서는 로그 부하로 인해 비활성화하는 것을 권장합니다.

**DB 서버 입장에서의 차이**

세 가지 방식이 DB 서버에서 어떻게 처리되는지 비교하면 성능 차이의 원인을 명확히 이해할 수 있습니다.

| 항목                | 단건 INSERT          | JDBC Batch INSERT | Multi-Value INSERT           |
|:------------------|:-------------------|:------------------|:-----------------------------|
| 방식                | 건당 1회 전송           | N개 구문을 1패킷으로 전송   | 1개 구문(`VALUES ...,...`)으로 전송 |
| 네트워크 왕복           | N번                 | 1번                | 1번                           |
| DB가 수신하는 구문 수     | N개                 | N개                | 1개                           |
| SQL 파싱 비용         | N번                 | N번                | 1번                           |
| 트랜잭션 커밋 오버헤드      | N번 (auto-commit 시) | 1번                | 1번                           |
| DB 내부 잠금(Lock) 획득 | N번                 | N번                | 1번                           |
| 인덱스 재계산           | 건마다 발생             | 건마다 발생            | 삽입 완료 후 일괄 처리 가능             |

* **단건 INSERT**: `saveAll`처럼 건마다 개별 커넥션 요청을 보내는 방식
* **JDBC Batch INSERT**: `addBatch()` + `executeBatch()`로 N개의 구문을 하나의 패킷에 묶어 전송하지만, DB는 여전히 N개의 구문을 각각 파싱·실행
* **Multi-Value INSERT**: `rewriteBatchedStatements=true`로 드라이버가 `INSERT INTO ... VALUES (...),(...),...` 형태의 단일 구문으로 변환하여 전송

JDBC Batch INSERT는 네트워크 왕복을 줄이는 효과가 있지만, DB 서버의 파싱·실행 횟수는 줄지 않습니다. Multi-Value INSERT는 **네트워크 왕복과 DB 파싱·실행을 모두 1번으로 줄이기** 때문에 데이터 양이 많을수록 차이가 커집니다.

### 성능 측정 결과 (Insert)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/Insert-Performance.png)

JPA `saveAll`과 QueryDSL `addBatch`를 사용했을 때의 성능 차이를 비교한 결과입니다. 데이터 개수가 늘어날수록 성능 차이가 확연하게 벌어지는 것을 확인할 수 있습니다.

| rows   | saveAll (ms) | add batch (ms) | 성능 개선율 |
|:-------|:-------------|:---------------|:-------|
| 100    | 104          | 12             | 88.50% |
| 200    | 174.5        | 16             | 90.80% |
| 500    | 370.25       | 27.5           | 92.60% |
| 1,000  | 695          | 56             | 91.90% |
| 2,000  | 1,574        | 68             | 95.70% |
| 5,000  | 3,778        | 140            | 96.30% |
| 10,000 | 7,505        | 265            | 96.50% |

* **saveAll**: JPA Repository의 saveAll 메서드 사용
* **add batch**: QueryDSL SQLQueryFactory의 addBatch 사용

10,000건 기준으로 약 **96.5%**의 성능 개선 효과가 있었습니다. `saveAll`이 약 7.5초 걸리는 작업을 Batch Insert로는 0.26초 만에 처리할 수 있습니다.

> **참고**: 이 측정은 애플리케이션 서버와 데이터베이스가 **동일한 로컬 환경(loopback)**에서 수행된 결과입니다. Loopback 통신은 실제 네트워크 대비 레이턴시가 거의 없는 이상적인 조건임에도 불구하고 이 정도의 성능 차이가 발생합니다. 실제 운영 환경처럼 애플리케이션 서버와 DB 서버가 **별도의 네트워크**에 위치한다면, 건별로 Insert 쿼리를 전송하는 `saveAll` 방식은 네트워크 왕복 비용이 Insert 건수만큼 누적되어 성능 차이가 훨씬 더 크게 벌어질 수 있습니다.

## Batch Update 적용

앞서 살펴본 Batch Insert 적용에서 QueryDSL-SQL로 Insert 성능을 개선하는 방법을 다뤘습니다. 이번에는 같은 접근을 Update에도 적용해보겠습니다. Insert와 동일하게 `addBatch` + `execute()` 패턴을 사용하지만, Update에는 수정 대상을 특정하는 `where` 조건이 반드시 필요하다는 차이가 있습니다.

### JPA Dirty Checking 의 한계

JPA의 일반적인 Update 패턴은 엔티티를 조회하여 영속성 컨텍스트에 올린 뒤, 필드를 변경하고 트랜잭션이 커밋될 때 변경 감지(Dirty Checking)를 통해 UPDATE 쿼리를 실행하는 방식입니다. 아래는 이번 성능 비교의 기준선(baseline)으로 사용하는 `updateWriters` 메서드입니다.

```kotlin
@Transactional
fun updateWriters(writers: List<Writer>) {
    for (writer in writers) {
        writer.name = "updated"
        writerRepository.save(writer) // dirty checking → 트랜잭션 커밋 시 건별 UPDATE 발생
    }
}
```

편리한 방식이지만, 1,000개의 데이터를 수정하면 1,000번의 UPDATE 쿼리가 데이터베이스로 전송됩니다. 대량 수정 작업에서는 이 점이 심각한 성능 저하의 원인이 됩니다.

### 구현: executeBulkUpdateWritersWithSql (WriterUpdate, name 1필드 + where(id.eq) 필수)

Update에서 Batch에 적재할 데이터는 `Writer` 엔티티 전체가 아니라, 수정에 필요한 필드만 담은 별도의 데이터 클래스로 표현합니다.

```kotlin
data class WriterUpdate(
    val id: Long,
    val name: String,
)
```

이 `WriterUpdate`를 받아 `name` 필드 하나만 갱신하는 것이 `executeBulkUpdateWritersWithSql`의 구현입니다.

```kotlin
@Transactional
fun executeBulkUpdateWritersWithSql(writers: List<WriterUpdate>): Long {
    // 1. 테이블 메타데이터 정의
    val writerTable = RelationalPathBase(Writer::class.java, "writer", null, "writer")
    // 2. SQLQueryFactory (공유 인스턴스 사용)
    val update = sqlQueryFactory.update(writerTable)
    // 3. 데이터를 Batch에 추가
    for (writer in writers) {
        val id = requireNotNull(writer.id) { "Writer id must not be null" }
        update
            .set(QWriter.writer.name, writer.name)
            .where(QWriter.writer.id.eq(id))
            .addBatch() // 메모리에 쿼리 적재
    }

    // 4. 일괄 실행
    return update.execute()
}
```

Batch Insert와 Batch Update는 동일한 `addBatch` + `execute()` 패턴을 사용하지만, 중요한 구조적 차이가 있습니다.

| 항목         | Batch Insert         | Batch Update              |
|:-----------|:---------------------|:--------------------------|
| `where` 조건 | 불필요                  | **필수**                    |
| 테이블 참조     | `RelationalPathBase` | `RelationalPathBase` (동일) |
| 컬럼 참조      | `QWriter.writer.*`   | `QWriter.writer.*` (동일)   |
| id 처리      | 없음                   | `requireNotNull` 필요       |

가장 중요한 차이는 **`where` 조건**입니다. `where` 없이 `addBatch()`를 호출하면 해당 UPDATE는 테이블 전체를 대상으로 실행되어 의도치 않은 전체 UPDATE가 발생할 수 있습니다. 반드시 `where(QWriter.writer.id.eq(id))`와 같이 대상 row를 특정해야 합니다. 또한 `id`는 nullable 타입이므로, `requireNotNull`로 null을 방어한 뒤 사용해야 컴파일 타입 안전성이 보장됩니다.

### profileSQL 로그로 batch 동작 검증

`addBatch`를 사용하면 SQL 내용 자체는 dirty checking과 동일하게 `UPDATE writer SET ... WHERE id = ?` 형태입니다. 쿼리 내용만으로는 실제로 batch가 동작하는지 구분하기 어렵습니다.

실제 전송 방식의 차이는 JDBC URL에 `profileSQL=true`를 추가하면 로그로 확인할 수 있습니다.

```
jdbc:mysql://localhost:3306/mydb?rewriteBatchedStatements=true&logger=Slf4JLogger&profileSQL=true
```

**케이스 1 — dirty checking (N번 통신)**

```
[QUERY] update writer set active=1,email='email-2',name='updated'... where id=2
        [at ProxyPreparedStatement.executeUpdate]
[FETCH] [at ProxyPreparedStatement.executeUpdate]
[QUERY] update writer set active=1,email='email-3',name='updated'... where id=3
        [at ProxyPreparedStatement.executeUpdate]
[FETCH] [at ProxyPreparedStatement.executeUpdate]
[QUERY] update writer set active=1,email='email-4',name='updated'... where id=4
        [at ProxyPreparedStatement.executeUpdate]
[FETCH] [at ProxyPreparedStatement.executeUpdate]
...
```

`executeUpdate`가 건마다 호출되어 `[QUERY] + [FETCH]` 쌍이 건수만큼 반복됩니다. 10건이면 DB 서버로 **10번 왕복**합니다.

> 로그에 `name` 외에 `active`, `email`도 함께 SET되는 것을 볼 수 있습니다. Hibernate는 `@DynamicUpdate`를 명시하지 않는 한 변경 감지 시 매핑된 컬럼 전체를 UPDATE 문에 포함하는 것이 기본 동작이기 때문입니다.

**케이스 2 — addBatch (1번 통신)**

```
[QUERY] update writer
set name = 'new'
where writer.id = 1;update writer
set name = 'new'
where writer.id = 2;update writer
set name = 'new'
where writer.id = 3;
...
update writer
set name = 'new'
where writer.id = 28;
```

`[QUERY]` 로그가 **1개**만 출력됩니다. 세미콜론으로 구분된 모든 쿼리가 하나의 패킷으로 전송되며, 28건이든 10,000건이든 DB 서버로 **1번만 왕복**합니다.

**구분 포인트 요약**

| 구분 기준 | dirty checking | addBatch |
|:--------|:--------------|:---------|
| 메서드명 | `executeUpdate` | `executeBatch` (`[QUERY]` 수로 판단) |
| `[QUERY]` 로그 수 | N개 | **1개** |
| `[FETCH]` 존재 | 건마다 존재 | 없음 |
| 쿼리 형태 | 쿼리 1개씩 | `;`로 이어진 멀티 쿼리 |

`profileSQL=true`만 붙이면 로그 줄 수와 메서드명만으로 의도한 대로 batch가 동작하고 있는지 즉시 확인할 수 있습니다.

### 성능 측정 결과 (Update)

JPA Dirty Checking과 QueryDSL `addBatch`를 사용했을 때의 성능 차이를 비교한 결과입니다.

![Update Performance](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/Update_Performance.svg)

| rows   | dirty checking (ms) | add batch (ms) | 성능 개선율 |
|:-------|:--------------------|:---------------|:-----------|
| 100    | 265.5               | 28.5           | 89.3%      |
| 200    | 368.5               | 45.5           | 87.7%      |
| 500    | 808.75              | 103.5          | 87.2%      |
| 1,000  | 1,647.25            | 191.5          | 88.4%      |
| 2,000  | 3,315.0             | 392.0          | 88.2%      |
| 5,000  | 8,593.5             | 928.25         | 89.2%      |
| 10,000 | 16,530.75           | 2,063.0        | 87.5%      |

* **bar (dirty checking)**: JPA `save(writer)`를 루프에서 건별 호출 — UPDATE N번 개별 전송
* **line (add batch)**: QueryDSL SQLQueryFactory의 addBatch — N개의 UPDATE를 1번의 네트워크 왕복으로 전송

10,000건 기준으로 약 **87.5%**, 1,000건 기준으로는 **88.4%**의 성능 개선 효과가 있었습니다.

> **참고**: 이 측정 역시 애플리케이션 서버와 데이터베이스가 **동일한 로컬 환경(loopback)**에서 수행된 결과입니다. Loopback 통신은 실제 네트워크 대비 레이턴시가 거의 없는 이상적인 조건임에도 불구하고 이 정도의 성능 차이가 발생합니다. 실제 운영 환경처럼 애플리케이션 서버와 DB 서버가 **별도의 네트워크**에 위치한다면, 건별로 전송하는 dirty checking 방식은 네트워크 왕복 비용이 쿼리 수만큼 누적되어 성능 차이가 훨씬 더 크게 벌어질 수 있습니다.

## (선택) 정리 — 두 실험의 회수

Insert는 `saveAll` 대비 10,000건 기준 96.5%, Update는 dirty checking 대비 10,000건 기준 87.5%(1,000건 기준 88.4%) 개선되었습니다. 두 수치를 나란히 놓고 "Insert가 Update보다 우수하다"고 결론짓고 싶어질 수 있지만, 이는 오도하는 비교입니다. Insert는 `saveAll`의 IDENTITY 단건 Insert를 기준선으로 삼고, Update는 dirty checking N-update를 기준선으로 삼는, **서로 다른 연산·서로 다른 기준선을 가진 두 개의 독립적인 실험**이기 때문입니다. 실제로 100건 구간만 보면 Update(89.3%)가 Insert(88.5%)보다 개선율이 더 높게 나오기도 합니다. 두 수치는 배수로 통일하거나 비교표로 나란히 놓지 않고, 각자의 절대값으로만 읽어야 합니다.

수치 비교 대신, 두 실험에서 공통으로 확인된 실무 체크리스트를 정리합니다.

* **트랜잭션 경계**: `addBatch()`로 쌓인 쿼리는 `execute()` 시점에 일괄 전송되므로, Insert/Update 모두 하나의 `@Transactional` 경계 안에서 처리되어야 합니다.
* **`rewriteBatchedStatements=true` 필수**: 이 옵션 없이는 `addBatch()`가 JDBC Batch 수준에 머물러, DB 파싱·실행 횟수를 줄이는 Multi-Value 효과를 얻지 못합니다.
* **Update는 `where` 절이 필수**: `where` 없는 `addBatch()`는 테이블 전체를 대상으로 UPDATE를 실행할 수 있는 위험한 코드입니다. 반드시 `id`와 같은 식별자로 대상 row를 특정해야 합니다.
* **`profileSQL=true`는 개발 환경 전용**: batch 동작을 검증하는 데는 유용하지만, 운영 환경에서는 로그 부하가 커지므로 비활성화해야 합니다.

## 마치며

지금까지 Batch Insert와 Batch Update를 QueryDSL-SQL로 각각 적용하고, 성능까지 확인해봤습니다. JPA의 `saveAll`과 Dirty Checking은 각각 편리한 API지만, 데이터 양이 늘어나는 순간 서로 다른 이유로 성능의 벽에 부딪힙니다. 두 경우 모두 QueryDSL-SQL의 `SQLQueryFactory`와 `addBatch`로 해결할 수 있었습니다. Insert는 `where` 없이 여러 row를 한 번에 밀어 넣고, Update는 `where`로 대상을 특정하며 여러 row를 한 번에 갱신한다는 차이가 있을 뿐, 근본적으로는 같은 패턴입니다.

대량의 데이터를 저장하거나 수정해야 하는 배치성 작업이 있다면, 새로운 라이브러리를 도입하기 전에 이미 프로젝트에 있는 QueryDSL-SQL을 먼저 검토해보시길 권장합니다.
