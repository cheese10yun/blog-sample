# QueryDSL을 이용한 Batch Update 성능 개선

JPA를 사용하다 보면 대량의 데이터를 수정해야 하는 상황에서 Dirty Checking 방식의 성능 한계에 부딪히게 됩니다. 이번 포스팅에서는 JPA Dirty Checking의 성능 이슈를 살펴보고, QueryDSL의 `SQLQueryFactory`를 활용한 Batch Update로 성능을 획기적으로 개선하는 방법을 소개합니다.

## 개요

대량의 데이터를 수정해야 할 때, 일반적으로 JPA의 Dirty Checking 방식을 사용합니다. 엔티티를 조회하여 영속성 컨텍스트에 올린 뒤 필드를 변경하면 트랜잭션 커밋 시점에 변경 감지가 일어나 UPDATE 쿼리가 실행되는 방식입니다. 하지만 데이터의 양이 늘어날수록 이 방식의 처리 속도는 급격히 느려질 수 있습니다. 엔티티 수만큼 개별적인 UPDATE 쿼리가 발생하기 때문입니다.

이전 포스팅([QueryDSL을 이용한 Batch Insert 성능 개선](https://cheese10yun.github.io/querydsl-batch-insert/))에서 Insert 성능 개선 방법을 소개한 적이 있습니다. Update도 동일한 접근으로 해결할 수 있습니다.

이미 프로젝트에서 JPA와 QueryDSL을 사용하고 있다면, 추가적인 라이브러리 도입 없이 **QueryDSL-SQL** 모듈을 활용하여 Type-Safe하게 Batch Update를 구현할 수 있습니다. 이번 포스팅에서는 그 방법을 소개합니다.

## JPA Dirty Checking의 성능 이슈

JPA의 일반적인 Update 패턴은 엔티티를 조회하여 영속성 컨텍스트에 올린 뒤, 필드를 변경하고 트랜잭션이 커밋될 때 변경 감지(Dirty Checking)를 통해 UPDATE 쿼리를 실행하는 방식입니다.

```kotlin
@Transactional
fun updateWriters(ids: List<Long>) {
    val writers = writerRepository.findAllById(ids)
    for (writer in writers) {
        writer.name = "updated"
        writer.score = 100
        // dirty checking → 트랜잭션 커밋 시 건별 UPDATE 발생
    }
}
```

위 코드는 사용하기 편리하지만, 1,000개의 데이터를 수정하면 1,000번의 UPDATE 쿼리가 데이터베이스로 전송됩니다. 여기에 `findAllById`로 인한 SELECT 쿼리까지 더하면, 대량 수정 작업에서는 심각한 성능 저하의 원인이 됩니다.

## QueryDSL Batch Update 구현

QueryDSL-SQL 모듈을 사용하면 JPA 엔티티가 아닌 JDBC 레벨에서 직접 SQL을 구성하여 실행할 수 있습니다. 이를 통해 `addBatch` 기능을 활용한 Bulk Update를 구현할 수 있습니다.

### 구현 코드 예시

`SQLQueryFactory`를 사용하여 Batch Update를 구현하는 방법은 다음과 같습니다.

```kotlin
@Service
class BatchInsertService(
    private val dataSource: DataSource
) {
    private val sqlQueryFactory: SQLQueryFactory by lazy {
        SQLQueryFactory(Configuration(MySQLTemplates()), dataSource)
    }

    @Transactional
    fun executeBulkUpdateWritersWithSql(writers: List<Writer>): Long {
        // 1. 테이블 메타데이터 정의
        val writerTable = RelationalPathBase(Writer::class.java, "writer", null, "writer")
        // 2. SQLQueryFactory (공유 인스턴스 사용)
        val update = sqlQueryFactory.update(writerTable)
        // 3. 데이터를 Batch에 추가
        for (writer in writers) {
            val id = requireNotNull(writer.id) { "Writer id must not be null" }
            update
                .set(QWriter.writer.name, writer.name)
                .set(QWriter.writer.email, writer.email)
                .set(QWriter.writer.score, writer.score)
                .set(QWriter.writer.reputation, writer.reputation)
                .set(QWriter.writer.active, writer.active)
                .where(QWriter.writer.id.eq(id))
                .addBatch() // 메모리에 쿼리 적재
        }

        // 4. 일괄 실행
        return update.execute()
    }
}
```

**코드 설명**

* **RelationalPathBase**: SQL 쿼리 작성을 위해 대상 테이블의 메타데이터를 정의합니다. `SQLQueryFactory`는 `RelationalPath` 타입을 요구하기 때문에, JPA 엔티티 기반의 `QWriter`(`EntityPathBase`)를 테이블 참조로 직접 사용할 수 없어 별도로 정의합니다.
* **QWriter.writer.***: 컬럼 참조에는 기존에 생성된 Q클래스의 path를 그대로 활용합니다.
* **addBatch()**: 루프를 돌며 데이터를 즉시 UPDATE 하지 않고, JDBC의 Batch 기능을 활용하기 위해 메모리에 쿼리 파라미터들을 쌓아둡니다.
* **execute()**: 쌓여있는 Batch 쿼리를 데이터베이스로 한 번에 전송하여 실행합니다.

### Insert와의 구조적 차이

Batch Insert와 Batch Update는 동일한 `addBatch` + `execute()` 패턴을 사용하지만, 중요한 구조적 차이가 있습니다.

| 항목         | Batch Insert         | Batch Update              |
|:-----------|:---------------------|:--------------------------|
| `where` 조건 | 불필요                  | **필수**                    |
| 테이블 참조     | `RelationalPathBase` | `RelationalPathBase` (동일) |
| 컬럼 참조      | `QWriter.writer.*`   | `QWriter.writer.*` (동일)   |
| id 처리      | 없음                   | `requireNotNull` 필요       |

가장 중요한 차이는 **`where` 조건**입니다. `where` 없이 `addBatch()`를 호출하면 해당 UPDATE는 테이블 전체를 대상으로 실행되어 의도치 않은 전체 UPDATE가 발생할 수 있습니다. 반드시 `where(QWriter.writer.id.eq(id))`와 같이 대상 row를 특정해야 합니다.

또한 `EntityAuditing`을 통해 상속받는 `id` 필드는 `Long?` (nullable) 타입이므로, `requireNotNull`로 null을 방어한 뒤 사용해야 컴파일 타입 안전성이 보장됩니다.

### MySQL 최적화 옵션: rewriteBatchedStatements

Insert 포스팅에서 소개한 `rewriteBatchedStatements=true` 옵션은 Batch Update에도 동일하게 적용됩니다.

```
jdbc:mysql://localhost:3306/mydb?rewriteBatchedStatements=true
```

이 옵션이 없으면 `addBatch()`로 적재된 쿼리들이 개별적으로 전송됩니다. 이 옵션을 활성화하면 드라이버 레벨에서 여러 UPDATE 구문을 묶어 전송하여 네트워크 비용을 줄일 수 있습니다.

## 성능 비교

### 성능 측정 코드

정확한 성능 측정을 위해 JPA Dirty Checking 방식과 QueryDSL `addBatch`의 실행 시간을 각각 측정했습니다.

```kotlin
@Test
fun `dirty checking update test`() {
    val rowsList = listOf(100, 200, 500, 1_000, 2_000, 5_000, 10_000)
    val iterations = 5

    rowsList.forEach { rows ->
        // 테스트 데이터 사전 삽입
        val writers = (1..rows).map { Writer(name = "name-$it", email = "email-$it") }
        writerRepository.saveAll(writers)

        var totalTimeMillis = 0.0
        for (i in 1..iterations) {
            val ids = writerRepository.findAll().map { it.id!! }

            val stopWatch = StopWatch()
            stopWatch.start()
            writerService.updateWriters(ids)
            stopWatch.stop()

            if (i > 1) { // 첫 회차 제외
                totalTimeMillis += stopWatch.totalTimeMillis
            }
        }
        val averageTimeMillis = totalTimeMillis / (iterations - 1)
        println("$rows 건 dirty checking 평균 실행 시간: ${averageTimeMillis} ms")
    }
}

@Test
fun `executeBulkUpdateWritersWithSql test`() {
    val rowsList = listOf(100, 200, 500, 1_000, 2_000, 5_000, 10_000)
    val iterations = 5

    rowsList.forEach { rows ->
        // 테스트 데이터 사전 삽입
        val savedWriters = writerRepository.saveAll(
            (1..rows).map { Writer(name = "name-$it", email = "email-$it") }
        )

        var totalTimeMillis = 0.0
        for (i in 1..iterations) {
            savedWriters.forEach { it.name = "updated-$i" }

            val stopWatch = StopWatch()
            stopWatch.start()
            batchInsertService.executeBulkUpdateWritersWithSql(savedWriters)
            stopWatch.stop()

            if (i > 1) { // 첫 회차 제외
                totalTimeMillis += stopWatch.totalTimeMillis
            }
        }
        val averageTimeMillis = totalTimeMillis / (iterations - 1)
        println("$rows 건 QueryDSL Batch Update 평균 실행 시간: ${averageTimeMillis} ms")
    }
}
```

**측정 방식 설명**

* **반복 측정**: 각 데이터 구간(100건 ~ 10,000건)마다 총 **5회** 반복하여 측정했습니다.
* **Warm-up 고려**: 테스트 실행 시 **첫 번째 회차는 결과에서 제외**했습니다. 커넥션 풀 초기화 등 초기 비용이 포함되어 결과가 왜곡되는 것을 방지하기 위함입니다.
* **평균값 산출**: 첫 회차를 제외한 나머지 **4회의 실행 시간**을 합산하여 평균값을 산출했습니다.

### 성능 측정 결과

JPA Dirty Checking과 QueryDSL `addBatch`를 사용했을 때의 성능 차이를 비교한 결과입니다.

| rows   | dirty checking (ms) | add batch (ms) | 성능 개선율 |
|:-------|:--------------------|:---------------|:-------|
| 100    | -                   | -              | -%     |
| 200    | -                   | -              | -%     |
| 500    | -                   | -              | -%     |
| 1,000  | -                   | -              | -%     |
| 2,000  | -                   | -              | -%     |
| 5,000  | -                   | -              | -%     |
| 10,000 | -                   | -              | -%     |

* **dirty checking**: JPA Dirty Checking 방식으로 엔티티 조회 후 필드 변경
* **add batch**: QueryDSL SQLQueryFactory의 addBatch 사용

## 결론

대량의 데이터를 수정해야 하는 배치성 작업에서는 JPA의 Dirty Checking 방식보다 JDBC Batch Update를 사용하는 것이 성능 측면에서 훨씬 유리합니다.

Insert와 마찬가지로, 오직 Batch Update 성능 개선만을 위해 별도의 라이브러리를 도입하는 것은 프로젝트의 복잡도를 높일 수 있습니다. 이미 JPA와 QueryDSL을 사용 중인 환경이라면, **QueryDSL-SQL**을 활용하는 것이 추가적인 학습 곡선이나 설정의 번거로움 없이 Type-Safe하게 성능을 극대화할 수 있는 가장 효율적인 대안입니다.

Insert와 Update 모두 동일한 `addBatch` + `execute()` 패턴을 사용하지만, Update에서는 반드시 `where` 조건으로 대상 row를 특정해야 한다는 점을 기억하세요.



100 건 dirty checking 평균 실행 시간: 235.5 ms
200 건 dirty checking 평균 실행 시간: 365.0 ms
500 건 dirty checking 평균 실행 시간: 806.5 ms
1000 건 dirty checking 평균 실행 시간: 1581.25 ms
2000 건 dirty checking 평균 실행 시간: 3408.5 ms
5000 건 dirty checking 평균 실행 시간: 8580.0 ms
10000 건 dirty checking 평균 실행 시간: 16940.25 ms



100 건 dirty checking 평균 실행 시간: 239.5 ms
200 건 dirty checking 평균 실행 시간: 453.75 ms
500 건 dirty checking 평균 실행 시간: 957.5 ms
1000 건 dirty checking 평균 실행 시간: 2018.75 ms
2000 건 dirty checking 평균 실행 시간: 3664.75 ms
5000 건 dirty checking 평균 실행 시간: 8446.0 ms
10000 건 dirty checking 평균 실행 시간: 17272.25 ms

